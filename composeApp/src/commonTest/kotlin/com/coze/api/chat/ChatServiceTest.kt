package com.coze.api.chat

import com.coze.api.model.chat.*
import com.coze.api.model.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 聊天服务测试类
 * 
 * 测试范围：
 * 1. 创建聊天
 * 2. 轮询聊天状态
 * 3. 提交工具输出
 * 4. 流式响应处理
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 使用 CIO 引擎处理 SSE 请求
 * 3. 验证请求参数的正确性
 * 4. 验证响应数据的解析
 * 5. 验证参数验证逻辑
 */
class ChatServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var chatService: ChatService
    private val requestsVerified = mutableListOf<String>()
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    @BeforeTest
    fun setup() {
        // 设置 Mock 客户端
        mockEngine = MockEngine { request ->
            requestsVerified.add(request.url.encodedPath)
            
            when (request.url.encodedPath) {
                "/v3/chat" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val createReq = json.decodeFromString<CreateChatReq>(requestBody)
                    assertEquals("test_bot_id", createReq.botId)
                    assertNotNull(createReq.additionalMessages)
                    assertEquals(1, createReq.additionalMessages?.size)
                    assertEquals("test message", createReq.additionalMessages?.get(0)?.content)
                    
                    if (createReq.stream == true) {
                        respond(
                            content = """
                                data: {"id":"test_chat_id","conversation_id":"test_conversation_id","status":"completed","event":"conversation.chat.completed"}

                                data: [DONE]
                                event: [DONE]
                            """.trimIndent(),
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "text/event-stream")
                        )
                    } else {
                        respond(
                            content = """{"code":0,"message":"success","data":{"id":"test_chat_id","conversation_id":"test_conversation_id","status":"completed"}}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                }
                "/v3/chat/retrieve" -> {
                    assertEquals("test_conversation_id", request.url.parameters["conversation_id"])
                    assertEquals("test_chat_id", request.url.parameters["chat_id"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"id":"test_chat_id","conversation_id":"test_conversation_id","status":"completed"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v3/chat/message/list" -> {
                    assertEquals("test_conversation_id", request.url.parameters["conversation_id"])
                    assertEquals("test_chat_id", request.url.parameters["chat_id"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":[{"id":"test_message_id","role":"assistant","content":"test response","type":"answer","conversation_id":"test_conversation_id","content_type":"text"}]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v3/chat/submit_tool_outputs" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    println("[TEST] Submit tool outputs request body: $requestBody")
                    val submitReq = json.decodeFromString<SubmitToolOutputsReq>(requestBody)
                    
                    if (!submitReq.stream) {
                        respond(
                            content = """{"code":0,"message":"success","data":{"id":"test_chat_id","conversation_id":"test_conversation_id","status":"completed"}}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    } else {
                        error("Stream requests should be handled by SSE handler")
                    }
                }
                "/v3/chat/cancel" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val bodyMap = json.decodeFromString<Map<String, String>>(requestBody)
                    assertEquals("test_conversation_id", bodyMap["conversation_id"])
                    assertEquals("test_chat_id", bodyMap["chat_id"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"id":"test_chat_id","conversation_id":"test_conversation_id","status":"canceled"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        
        // 初始化 chatService
        chatService = ChatService()
        chatService.setClient(mockEngine)

    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
//        APIClient.closeHandlers()
    }

    /**
     * 测试创建聊天
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证空 botId 参数校验
     */
    @Test
    fun testCreateChat() = runTest {
        val request = CreateChatReq(
            botId = "test_bot_id",
            additionalMessages = listOf(
                EnterMessage(
                    role = RoleType.USER,
                    content = "test message"
                )
            )
        )
        val response = chatService.createChat(request)
        
        assertNotNull(response.data)
        assertEquals("test_chat_id", response.data?.id)
        assertEquals("test_conversation_id", response.data?.conversationId)
        assertEquals(ChatStatus.COMPLETED, response.data?.status)
        assertTrue(requestsVerified.contains("/v3/chat"))

        assertFailsWith<IllegalArgumentException> {
            chatService.createChat(request.copy(botId = ""))
        }
    }

    /**
     * 测试创建并轮询聊天
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证轮询逻辑
     */
    @Test
    fun testCreateAndPollChat() = runTest {
        val request = CreateChatReq(
            botId = "test_bot_id",
            additionalMessages = listOf(
                EnterMessage(
                    role = RoleType.USER,
                    content = "test message"
                )
            )
        )
        val response = chatService.createAndPollChat(request)
        
        assertNotNull(response.chat)
        assertEquals("test_chat_id", response.chat.id)
        assertEquals("test_conversation_id", response.chat.conversationId)
        assertEquals(ChatStatus.COMPLETED, response.chat.status)
        assertTrue(requestsVerified.contains("/v3/chat"))
        assertTrue(requestsVerified.contains("/v3/chat/retrieve"))
        assertTrue(requestsVerified.contains("/v3/chat/message/list"))

        assertFailsWith<IllegalArgumentException> {
            chatService.createAndPollChat(request.copy(botId = ""))
        }
    }

    /**
     * 测试提交工具输出
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testSubmitToolOutputs() = runTest {
        val request = SubmitToolOutputsReq(
            conversationId = "test_conversation_id",
            chatId = "test_chat_id",
            toolOutputs = listOf(
                ToolOutputType(
                    toolCallId = "test_tool_call_id",
                    output = "test output"
                )
            ),
            stream = false
        )
        val response = chatService.submitToolOutputs(request)
        
        assertNotNull(response.data)
        assertEquals("test_chat_id", response.data?.id)
        assertEquals("test_conversation_id", response.data?.conversationId)
        assertTrue(requestsVerified.contains("/v3/chat/submit_tool_outputs"))

        assertFailsWith<IllegalArgumentException> {
            chatService.submitToolOutputs(request.copy(conversationId = ""))
        }

        assertFailsWith<IllegalArgumentException> {
            chatService.submitToolOutputs(request.copy(chatId = ""))
        }

        assertFailsWith<IllegalArgumentException> {
            chatService.submitToolOutputs(request.copy(toolOutputs = emptyList()))
        }
    }

    /**
     * 测试取消聊天
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testCancelChat() = runTest {
        val response = chatService.cancelChat(
            conversationId = "test_conversation_id",
            chatId = "test_chat_id"
        )
        assertNotNull(response.data)
        assertEquals("test_chat_id", response.data?.id)
        assertEquals("test_conversation_id", response.data?.conversationId)
        assertEquals(ChatStatus.CANCELED, response.data?.status)
        assertTrue(requestsVerified.contains("/v3/chat/cancel"))

        assertFailsWith<IllegalArgumentException> {
            chatService.cancelChat(
                conversationId = "",
                chatId = "test_chat_id"
            )
        }

        assertFailsWith<IllegalArgumentException> {
            chatService.cancelChat(
                conversationId = "test_conversation_id",
                chatId = ""
            )
        }
    }

    /**
     * 测试流式聊天
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     * 4. 验证流式响应处理
     */
    @Test
    @Ignore
    fun testStream() = runTest {
        val request = StreamChatReq(
            botId = "test_bot_id",
            additionalMessages = listOf(
                EnterMessage(
                    role = RoleType.USER,
                    content = "test message"
                )
            ),
            stream = false
        )
        
        var eventCount = 0
        chatService.stream(request).collect {
            eventCount++
            when (it) {
                is StreamChatData.CreateChatEvent -> {
                    assertEquals("test_chat_id", it.data.id)
                    assertEquals("test_conversation_id", it.data.conversationId)
                    assertEquals(ChatStatus.COMPLETED, it.data.status)
                }
                is StreamChatData.DoneEvent -> {
                    assertEquals("[DONE]", it.data)
                }
                else -> fail("Unexpected event type: $it")
            }
        }
        assertEquals(2, eventCount)
        assertTrue(requestsVerified.contains("/v3/chat"))

        assertFailsWith<IllegalArgumentException> {
            chatService.stream(request.copy(botId = ""))
        }

        assertFailsWith<IllegalArgumentException> {
            chatService.stream(request.copy(additionalMessages = emptyList()))
        }
    }
} 