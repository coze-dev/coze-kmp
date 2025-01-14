package com.coze.api.workflow

import com.coze.api.model.workflow.*
import com.coze.api.model.*
import com.coze.api.helper.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.sse.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 工作流服务测试类
 * 
 * 测试范围：
 * 1. 启动工作流运行
 * 2. 流式传输工作流运行事件
 * 3. 恢复暂停的工作流运行
 * 4. ChatFlow 功能
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证参数验证逻辑
 */
class WorkflowServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var workflowService: WorkflowService
    private val requestsVerified = mutableListOf<String>()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    @BeforeTest
    fun setup() {
        mockEngine = MockEngine { request ->
            requestsVerified.add(request.url.encodedPath)
            
            when (request.url.encodedPath) {
                "/v1/workflow/run" -> {
                    // 验证创建请求
                    val requestBody = request.body.toByteArray().decodeToString()
                    val runReq = json.decodeFromString<RunWorkflowReq>(requestBody)
                    assertEquals("test_workflow_id", runReq.workflowId)
                    
                    respond(
                        content = """{"data":"test_data","cost":"0.1","token":100,"debug_url":"https://example.com/debug"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/workflow/stream_run" -> {
                    // 验证流式请求
                    val requestBody = request.body.toByteArray().decodeToString()
                    val runReq = json.decodeFromString<RunWorkflowReq>(requestBody)
                    assertEquals("test_workflow_id", runReq.workflowId)
                    assertTrue(runReq.stream)
                    
                    respond(
                        content = """
                            data: {"event":"workflow.message","data":{"node_title":"Test Node","content":"Test message"}}
                            
                            data: {"event":"workflow.done","data":{"debug_url":"https://example.com/debug"}}
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
                    )
                }
                "/v1/workflow/stream_resume" -> {
                    // 验证恢复请求
                    val requestBody = request.body.toByteArray().decodeToString()
                    val resumeReq = json.decodeFromString<ResumeWorkflowReq>(requestBody)
                    assertEquals("test_workflow_id", resumeReq.workflowId)
                    assertEquals("test_event_id", resumeReq.eventId)
                    
                    respond(
                        content = """{"code":0,"message":"success","data":null}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/workflows/chat" -> {
                    // 验证 ChatFlow 请求
                    val requestBody = request.body.toByteArray().decodeToString()
                    val chatReq = json.decodeFromString<ChatWorkflowReq>(requestBody)
                    assertEquals("test_workflow_id", chatReq.workflowId)
                    assertNotNull(chatReq.additionalMessages)
                    assertTrue(chatReq.additionalMessages.isNotEmpty())
                    
                    respond(
                        content = """
                            data: {"event":"workflow.message","data":{"node_title":"Test Node","content":"Test message"}}
                            
                            data: {"event":"chat.message","data":{"content":"Test chat message"}}
                            
                            data: {"event":"workflow.done","data":{"debug_url":"https://example.com/debug"}}
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        
        workflowService = WorkflowService()
        workflowService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试启动工作流运行
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testCreate() = runTest {
        val request = RunWorkflowReq(
            workflowId = "test_workflow_id",
            stream = false
        )
        val response = workflowService.create(request)
        
        assertEquals("test_data", response.data)
        assertEquals("0.1", response.cost)
        assertEquals(100, response.token)
        assertEquals("https://example.com/debug", response.debugUrl)
        assertTrue(requestsVerified.contains("/v1/workflow/run"))

        assertFailsWith<IllegalArgumentException> {
            workflowService.create(request.copy(workflowId = ""))
        }
    }

    /**
     * 测试流式传输工作流运行事件
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证事件流处理
     * 3. 验证参数校验
     */
    @Test
    @Ignore
    fun testStream() = runTest {
        val request = RunWorkflowReq(
            workflowId = "test_workflow_id",
            stream = true
        )
        
        var messageReceived = false
        var doneReceived = false
        
        workflowService.stream(request).collect { event ->
            when (event) {
                is WorkflowStreamData.MessageEvent -> {
                    assertEquals("Test Node", event.data.nodeTitle)
                    assertEquals("Test message", event.data.content)
                    messageReceived = true
                }
                is WorkflowStreamData.DoneEvent -> {
                    assertEquals("https://example.com/debug", event.data.debugUrl)
                    doneReceived = true
                }
                else -> fail("Unexpected event type: $event")
            }
        }
        
        assertTrue(messageReceived)
        assertTrue(doneReceived)
        assertTrue(requestsVerified.contains("/v1/workflow/stream_run"))

        assertFailsWith<IllegalArgumentException> {
            workflowService.stream(request.copy(workflowId = ""))
        }
    }

    /**
     * 测试恢复暂停的工作流运行
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testResume() = runTest {
        val request = ResumeWorkflowReq(
            workflowId = "test_workflow_id",
            eventId = "test_event_id",
            resumeData = "test_data",
            interruptType = 1
        )
        val response = workflowService.resume(request)
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertTrue(requestsVerified.contains("/v1/workflow/stream_resume"))

        assertFailsWith<IllegalArgumentException> {
            workflowService.resume(request.copy(workflowId = ""))
        }

        assertFailsWith<IllegalArgumentException> {
            workflowService.resume(request.copy(eventId = ""))
        }
    }

    /**
     * 测试 ChatFlow 功能
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证事件流处理
     * 3. 验证参数校验
     */
    @Test
    @Ignore
    fun testChat() = runTest {
        val request = ChatWorkflowReq(
            workflowId = "test_workflow_id",
            additionalMessages = listOf(
                EnterMessage(
                    role = RoleType.USER,
                    content = "test message"
                )
            )
        )
        
        var workflowMessageReceived = false
        var chatMessageReceived = false
        var doneReceived = false
        
        workflowService.chat(request).collect { event ->
            when (event) {
                is ChatFlowData.WorkflowEvent -> {
                    when (val workflowData = event.data) {
                        is WorkflowStreamData.MessageEvent -> {
                            assertEquals("Test Node", workflowData.data.nodeTitle)
                            assertEquals("Test message", workflowData.data.content)
                            workflowMessageReceived = true
                        }
                        is WorkflowStreamData.DoneEvent -> {
                            assertEquals("https://example.com/debug", workflowData.data.debugUrl)
                            doneReceived = true
                        }
                        else -> {}
                    }
                }
                is ChatFlowData.ChatEvent -> {
                    when (val chatData = event.data) {
                        is StreamChatData.ChatMessageEvent -> {
                            assertEquals("Test chat message", chatData.data.content)
                            chatMessageReceived = true
                        }
                        else -> {}
                    }
                }
            }
        }
        
        assertTrue(workflowMessageReceived)
        assertTrue(chatMessageReceived)
        assertTrue(doneReceived)
        assertTrue(requestsVerified.contains("/v1/workflows/chat"))

        assertFailsWith<IllegalArgumentException> {
            workflowService.chat(request.copy(workflowId = ""))
        }

        assertFailsWith<IllegalArgumentException> {
            workflowService.chat(request.copy(additionalMessages = emptyList()))
        }
    }
} 