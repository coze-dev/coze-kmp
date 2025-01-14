package com.coze.api.conversation

import com.coze.api.model.EnterMessage
import com.coze.api.model.RoleType
import com.coze.api.model.conversation.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 会话服务测试类
 * 
 * 测试范围：
 * 1. 创建会话
 * 2. 获取会话列表
 * 3. 清空会话
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证参数验证逻辑
 */
class ConversationServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var conversationService: ConversationService
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
                "/v1/conversation/create" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val createReq = json.decodeFromString<CreateConversationReq>(requestBody)
                    assertEquals("test_bot_id", createReq.botId)
                    
                    respond(
                        content = """
                        {
                            "code": 0,
                            "message": "success",
                            "data": {
                                "id": "test_conversation_id",
                                "created_at": 1704067200,
                                "meta_data": {},
                                "conversation_id": "test_conversation_id",
                                "content_type": "text",
                                "role": "user",
                                "content": "test message"
                            }
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/conversations" -> {
                    assertEquals("test_bot_id", request.url.parameters["bot_id"])
                    assertEquals("50", request.url.parameters["page_size"])
                    assertEquals("1", request.url.parameters["page_num"])
                    
                    respond(
                        content = """
                        {
                            "code": 0,
                            "message": "success",
                            "data": {
                                "conversations": [
                                    {
                                        "id": "test_conversation_id",
                                        "created_at": 1704067200,
                                        "meta_data": {},
                                        "conversation_id": "test_conversation_id",
                                        "content_type": "text",
                                        "role": "user",
                                        "content": "test message"
                                    }
                                ],
                                "has_more": false
                            }
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/conversation/retrieve" -> {
                    assertEquals("test_conversation_id", request.url.parameters["conversation_id"])
                    
                    respond(
                        content = """
                        {
                            "code": 0,
                            "message": "success",
                            "data": {
                                "id": "test_conversation_id",
                                "created_at": 1704067200,
                                "meta_data": {},
                                "last_section_id": "test_section_id"
                            }
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/conversations/test_conversation_id/clear" -> {
                    respond(
                        content = """
                        {
                            "code": 0,
                            "message": "success",
                            "data": {
                                "id": "test_session_id",
                                "conversation_id": "test_conversation_id",
                                "content_type": "text",
                                "role": "user",
                                "content": "test message"
                            }
                        }
                        """.trimIndent(),
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        conversationService = ConversationService()
        conversationService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试创建会话
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证空 botId 参数校验
     */
    @Test
    fun testCreateConversation() = runTest {
        val request = CreateConversationReq(
            botId = "test_bot_id",
            messages = listOf(
                EnterMessage(
                    role = RoleType.USER,
                    content = "Hello"
                )
            )
        )
        val response = conversationService.create(request)
        
        assertNotNull(response.data)
        assertEquals("test_conversation_id", response.data?.id)
        assertEquals(1704067200, response.data?.createdAt)
        assertTrue(requestsVerified.contains("/v1/conversation/create"))

        assertFailsWith<IllegalArgumentException> {
            conversationService.create(request.copy(botId = ""))
        }
    }

    /**
     * 测试获取会话列表
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证空 botId 参数校验
     */
    @Test
    fun testListConversations() = runTest {
        val request = ListConversationReq(
            botId = "test_bot_id"
        )
        val response = conversationService.list(request)
        
        assertNotNull(response.data)
        assertEquals(1, response.data?.conversations?.size)
        assertEquals("test_conversation_id", response.data?.conversations?.get(0)?.id)
        assertEquals(1704067200, response.data?.conversations?.get(0)?.createdAt)
        assertFalse(response.data?.hasMore ?: true)
        assertTrue(requestsVerified.contains("/v1/conversations"))

        assertFailsWith<IllegalArgumentException> {
            conversationService.list(request.copy(botId = ""))
        }
    }

    /**
     * 测试清空会话
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证空 conversationId 参数校验
     */
    @Test
    fun testClearConversation() = runTest {
        val response = conversationService.clear("test_conversation_id")
        
        assertNotNull(response.data)
        assertEquals("test_session_id", response.data?.id)
        assertEquals("test_conversation_id", response.data?.conversationId)
        assertTrue(requestsVerified.contains("/v1/conversations/test_conversation_id/clear"))

        assertFailsWith<IllegalArgumentException> {
            conversationService.clear("")
        }
    }

    /**
     * 测试获取会话详情
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证空 conversationId 参数校验
     */
    @Test
    fun testRetrieve() = runTest {
        val response = conversationService.retrieve("test_conversation_id")
        
        assertNotNull(response.data)
        assertEquals("test_conversation_id", response.data?.id)
        assertEquals(1704067200, response.data?.createdAt)
        assertEquals("test_section_id", response.data?.lastSectionId)
        assertTrue(requestsVerified.contains("/v1/conversation/retrieve"))

        assertFailsWith<IllegalArgumentException> {
            conversationService.retrieve("")
        }
    }
} 