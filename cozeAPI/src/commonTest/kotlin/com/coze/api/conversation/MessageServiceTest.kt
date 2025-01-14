package com.coze.api.conversation

import com.coze.api.model.ContentType
import com.coze.api.model.RoleType
import com.coze.api.model.conversation.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 会话消息服务测试类
 * 
 * 测试范围：
 * 1. 创建消息
 * 2. 更新消息
 * 3. 删除消息
 * 4. 获取消息列表
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证参数验证逻辑
 */
class MessageServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var messageService: MessageService
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
                "/v1/conversation/message/create" -> {
                    assertEquals("test_conversation_id", request.url.parameters["conversation_id"])
                    val requestBody = request.body.toByteArray().decodeToString()
                    val createReq = json.decodeFromString<CreateMessageReq>(requestBody)
                    assertEquals(RoleType.USER, createReq.role)
                    assertEquals("test message", createReq.content)
                    assertEquals(ContentType.TEXT, createReq.contentType)
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"id":"test_message_id","role":"user","content":"test message","content_type":"text","conversation_id":"test_conversation_id"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/conversation/message/modify" -> {
                    assertEquals("test_conversation_id", request.url.parameters["conversation_id"])
                    assertEquals("test_message_id", request.url.parameters["message_id"])
                    val requestBody = request.body.toByteArray().decodeToString()
                    val updateReq = json.decodeFromString<UpdateMessageReq>(requestBody)
                    assertEquals("updated message", updateReq.content)
                    assertEquals(ContentType.TEXT, updateReq.contentType)
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"id":"test_message_id","role":"user","content":"updated message","content_type":"text","conversation_id":"test_conversation_id"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/conversation/message/delete" -> {
                    assertEquals("test_conversation_id", request.url.parameters["conversation_id"])
                    assertEquals("test_message_id", request.url.parameters["message_id"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"id":"test_message_id","role":"user","content":"test message","type":"text","conversation_id":"test_conversation_id","content_type":"text"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        messageService = MessageService()
        messageService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试创建消息
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testCreateMessage() = runTest {
        val request = CreateMessageReq(
            role = RoleType.USER,
            content = "test message",
            contentType = ContentType.TEXT
        )
        val response = messageService.create("test_conversation_id", request)
        
        assertNotNull(response.data)
        assertEquals("test_message_id", response.data?.id)
        assertEquals(RoleType.USER, response.data?.role)
        assertEquals("test message", response.data?.content)
        assertEquals(ContentType.TEXT, response.data?.contentType)
        assertEquals("test_conversation_id", response.data?.conversationId)
        assertTrue(requestsVerified.contains("/v1/conversation/message/create"))

        assertFailsWith<IllegalArgumentException> {
            messageService.create("", request)
        }
    }

    /**
     * 测试更新消息
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testUpdateMessage() = runTest {
        val request = UpdateMessageReq(
            content = "updated message",
            contentType = ContentType.TEXT
        )
        val response = messageService.update(
            conversationId = "test_conversation_id",
            messageId = "test_message_id",
            request = request
        )
        
        assertNotNull(response.data)
        assertEquals("test_message_id", response.data?.id)
        assertEquals(RoleType.USER, response.data?.role)
        assertEquals("updated message", response.data?.content)
        assertEquals(ContentType.TEXT, response.data?.contentType)
        assertEquals("test_conversation_id", response.data?.conversationId)
        assertTrue(requestsVerified.contains("/v1/conversation/message/modify"))

        assertFailsWith<IllegalArgumentException> {
            messageService.update(
                conversationId = "",
                messageId = "test_message_id",
                request = request
            )
        }

        assertFailsWith<IllegalArgumentException> {
            messageService.update(
                conversationId = "test_conversation_id",
                messageId = "",
                request = request
            )
        }
    }

    /**
     * 测试删除消息
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testDeleteMessage() = runTest {
        val response = messageService.delete(
            conversationId = "test_conversation_id",
            messageId = "test_message_id"
        )
        
        assertNotNull(response.data)
        assertEquals("test_message_id", response.data?.id)
        assertEquals(RoleType.USER, response.data?.role)
        assertEquals("test message", response.data?.content)
        assertEquals(ContentType.TEXT, response.data?.contentType)
        assertEquals("test_conversation_id", response.data?.conversationId)
        assertTrue(requestsVerified.contains("/v1/conversation/message/delete"))

        assertFailsWith<IllegalArgumentException> {
            messageService.delete(
                conversationId = "",
                messageId = "test_message_id"
            )
        }

        assertFailsWith<IllegalArgumentException> {
            messageService.delete(
                conversationId = "test_conversation_id",
                messageId = ""
            )
        }
    }
} 