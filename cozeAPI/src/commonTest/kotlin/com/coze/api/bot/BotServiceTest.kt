package com.coze.api.bot

import com.coze.api.model.bot.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.client.request.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * BotService 的单元测试类
 * 使用 MockEngine 模拟 HTTP 请求，验证请求参数和响应处理
 */
class BotServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var botService: BotService
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
                "/v1/bot/create" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val createReq = json.decodeFromString<CreateBotReq>(requestBody)
                    assertEquals("test_space_id", createReq.spaceId)
                    assertEquals("Test Bot", createReq.name)
                    assertEquals("Test Description", createReq.description)
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"bot_id":"test_bot_id"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/bot/update" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val updateReq = json.decodeFromString<UpdateBotReq>(requestBody)
                    assertEquals("test_bot_id", updateReq.botId)
                    assertEquals("Updated Bot", updateReq.name)
                    assertEquals("Updated Description", updateReq.description)
                    
                    respond(
                        content = """{"code":0,"message":"success","data":null}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/space/published_bots_list" -> {
                    assertEquals("test_space_id", request.url.parameters["space_id"])
                    assertEquals("10", request.url.parameters["page_size"])
                    assertEquals("1", request.url.parameters["page"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"total":1,"space_bots":[{"bot_id":"test_bot_id","bot_name":"Test Bot","description":"Test Description","icon_url":"https://test.com/icon.png","publish_time":"2024-01-01T00:00:00Z"}]}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/bot/publish" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val publishReq = json.decodeFromString<PublishBotReq>(requestBody)
                    assertEquals("test_bot_id", publishReq.botId)
                    assertEquals(listOf("1024"), publishReq.connectorIds)
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"bot_id":"test_bot_id","version":"1.0.0"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/bot/get_online_info" -> {
                    assertEquals("test_bot_id", request.url.parameters["bot_id"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"bot_id":"test_bot_id","name":"Test Bot","description":"Test Description","icon_url":"https://test.com/icon.png","create_time":1704067200,"update_time":1704067200,"version":"1.0.0","prompt_info":{"prompt":"Test Prompt"},"onboarding_info":{"prologue":"Test Prologue"},"bot_mode":1,"plugin_info_list":[],"model_info":{"model_id":"test_model","model_name":"Test Model"}}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        botService = BotService()
        botService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试创建智能体
     * 
     * 测试场景：
     * 1. 成功创建智能体
     *    - 验证请求参数正确（spaceId, name, description）
     *    - 验证返回的 botId 正确
     * 2. 参数验证失败
     *    - 验证空的 spaceId 会抛出异常
     */
    @Test
    fun testCreateBot() = runTest {
        val request = CreateBotReq(
            spaceId = "test_space_id",
            name = "Test Bot",
            description = "Test Description"
        )
        val response = botService.create(request)
        
        assertNotNull(response.data)
        assertEquals("test_bot_id", response.data?.botId)
        assertTrue(requestsVerified.contains("/v1/bot/create"))

        // 测试错误情况 - 空的 space_id
        val invalidRequest = CreateBotReq(
            spaceId = "",
            name = "Test Bot"
        )
        assertFailsWith<IllegalArgumentException> {
            botService.create(invalidRequest)
        }
    }

    /**
     * 测试更新智能体配置
     * 
     * 测试场景：
     * 1. 成功更新智能体配置
     *    - 验证请求参数正确（botId, name, description）
     *    - 验证请求路径正确
     * 2. 参数验证失败
     *    - 验证空的 botId 会抛出异常
     */
    @Test
    fun testUpdateBot() = runTest {
        val request = UpdateBotReq(
            botId = "test_bot_id",
            name = "Updated Bot",
            description = "Updated Description"
        )
        botService.update(request)
        assertTrue(requestsVerified.contains("/v1/bot/update"))

        val invalidRequest = UpdateBotReq(
            botId = "",
            name = "Updated Bot"
        )
        assertFailsWith<IllegalArgumentException> {
            botService.update(invalidRequest)
        }
    }

    /**
     * 测试获取智能体列表
     * 
     * 测试场景：
     * 1. 成功获取智能体列表
     *    - 验证查询参数正确（spaceId, pageSize, page）
     *    - 验证返回的列表数据正确（total, spaceBots）
     * 2. 参数验证失败
     *    - 验证空的 spaceId 会抛出异常
     */
    @Test
    fun testListBots() = runTest {
        val request = ListBotReq(
            spaceId = "test_space_id",
            pageSize = 10,
            page = 1
        )
        val response = botService.list(request)
        
        assertNotNull(response.data)
        assertEquals(1, response.data?.total)
        assertEquals(1, response.data?.spaceBots?.size)
        assertEquals("test_bot_id", response.data?.spaceBots?.get(0)?.botId)
        assertTrue(requestsVerified.contains("/v1/space/published_bots_list"))

        val invalidRequest = ListBotReq(
            spaceId = ""
        )
        assertFailsWith<IllegalArgumentException> {
            botService.list(invalidRequest)
        }
    }

    /**
     * 测试发布智能体为 API 服务
     * 
     * 测试场景：
     * 1. 成功发布智能体
     *    - 验证请求参数正确（botId, connectorIds）
     *    - 验证返回的版本信息正确
     * 2. 参数验证失败
     *    - 验证空的 botId 会抛出异常
     */
    @Test
    fun testPublishBot() = runTest {
        val request = PublishBotReq(
            botId = "test_bot_id",
            connectorIds = listOf("1024")
        )
        val response = botService.publish(request)
        
        assertNotNull(response.data)
        assertEquals("test_bot_id", response.data?.botId)
        assertEquals("1.0.0", response.data?.version)
        assertTrue(requestsVerified.contains("/v1/bot/publish"))

        val invalidRequest = PublishBotReq(
            botId = "",
            connectorIds = listOf("1024")
        )
        assertFailsWith<IllegalArgumentException> {
            botService.publish(invalidRequest)
        }
    }

    /**
     * 测试获取智能体配置信息
     * 
     * 测试场景：
     * 1. 成功获取智能体配置
     *    - 验证查询参数正确（botId）
     *    - 验证返回的配置信息正确（name, description 等）
     * 2. 参数验证失败
     *    - 验证空的 botId 会抛出异常
     */
    @Test
    fun testRetrieveBot() = runTest {
        val request = RetrieveBotReq(
            botId = "test_bot_id"
        )
        val response = botService.retrieve(request)
        
        assertNotNull(response.data)
        assertEquals("test_bot_id", response.data?.botId)
        assertEquals("Test Bot", response.data?.name)
        assertEquals("Test Description", response.data?.description)
        assertTrue(requestsVerified.contains("/v1/bot/get_online_info"))

        val invalidRequest = RetrieveBotReq(
            botId = ""
        )
        assertFailsWith<IllegalArgumentException> {
            botService.retrieve(invalidRequest)
        }
    }
} 