package com.coze.api.chat

import com.coze.api.model.ContentType
import com.coze.api.model.MessageType
import com.coze.api.model.RoleType
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 聊天消息服务测试类
 * 
 * 测试范围：
 * 1. 获取聊天消息列表
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
                "/v3/chat/message/list" -> {
                    assertEquals("test_conversation_id", request.url.parameters["conversation_id"])
                    assertEquals("test_chat_id", request.url.parameters["chat_id"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":[{"id":"test_message_id","role":"assistant","content":"test response","type":"answer","conversation_id":"test_conversation_id","content_type":"text"}]}""",
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
     * 测试获取聊天消息列表
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testListMessages() = runTest {
        val response = messageService.list(
            conversationId = "test_conversation_id",
            chatId = "test_chat_id"
        )
        
        assertNotNull(response.data)
        assertEquals(1, response.data?.size)
        assertEquals("test_message_id", response.data?.get(0)?.id)
        assertEquals(RoleType.ASSISTANT, response.data?.get(0)?.role)
        assertEquals("test response", response.data?.get(0)?.content)
        assertEquals(MessageType.ANSWER, response.data?.get(0)?.type)
        assertEquals("test_conversation_id", response.data?.get(0)?.conversationId)
        assertEquals(ContentType.TEXT, response.data?.get(0)?.contentType)
        assertTrue(requestsVerified.contains("/v3/chat/message/list"))

        assertFailsWith<IllegalArgumentException> {
            messageService.list(
                conversationId = "",
                chatId = "test_chat_id"
            )
        }

        assertFailsWith<IllegalArgumentException> {
            messageService.list(
                conversationId = "test_conversation_id",
                chatId = ""
            )
        }
    }
} 