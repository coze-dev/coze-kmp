package com.coze.api.dataset

import com.coze.api.model.dataset.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 图片服务测试类
 * 
 * 测试范围：
 * 1. 更新图片信息
 * 2. 获取图片列表
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证参数验证逻辑
 */
class ImageServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var imageService: ImageService
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
                "/v1/datasets/test_dataset_id/images/test_document_id" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val bodyMap = json.decodeFromString<UpdateImageRequest>(requestBody)
                    assertEquals("test caption", bodyMap.caption)
                    
                    respond(
                        content = """{"code":0,"message":"success"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/datasets/test_dataset_id/images" -> {
                    assertEquals("test_keyword", request.url.parameters["keyword"])
                    assertEquals("true", request.url.parameters["has_caption"])
                    assertEquals("1", request.url.parameters["page_num"])
                    assertEquals("10", request.url.parameters["page_size"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"total_count":1,"photo_infos":[{"document_id":"test_document_id","name":"test_image","url":"https://example.com/test.png","caption":"test caption","size":1024,"type":"png","status":1,"create_time":1704067200,"update_time":1704067200}]}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        imageService = ImageService()
        imageService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试更新图片信息
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testUpdateImage() = runTest {
        val response = imageService.update(
            datasetId = "test_dataset_id",
            documentId = "test_document_id",
            caption = "test caption"
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertTrue(requestsVerified.contains("/v1/datasets/test_dataset_id/images/test_document_id"))

        assertFailsWith<IllegalArgumentException> {
            imageService.update(
                datasetId = "",
                documentId = "test_document_id",
                caption = "test caption"
            )
        }
    }

    /**
     * 测试获取图片列表
     * 
     * 测试点：
     * 1. 验证分页参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testListImages() = runTest {
        val response = imageService.list(
            datasetId = "test_dataset_id",
            keyword = "test_keyword",
            hasCaption = true,
            pageNum = 1,
            pageSize = 10
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertEquals(1, response.data?.totalCount)
        assertEquals(1, response.data?.photoInfos?.size)
        assertEquals("test_document_id", response.data?.photoInfos?.get(0)?.documentId)
        assertTrue(requestsVerified.contains("/v1/datasets/test_dataset_id/images"))

        assertFailsWith<IllegalArgumentException> {
            imageService.list(
                datasetId = "",
                keyword = "test_keyword",
                hasCaption = true,
                pageNum = 1,
                pageSize = 10
            )
        }
    }
} 