package com.coze.api.dataset

import com.coze.api.model.dataset.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 数据集服务测试类
 * 
 * 测试范围：
 * 1. 创建数据集
 * 2. 更新数据集
 * 3. 删除数据集
 * 4. 获取数据集列表
 * 5. 处理数据集文档
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证参数验证逻辑
 */
class DatasetServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var datasetService: DatasetService
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
                "/v1/datasets" -> {
                    if (request.method == HttpMethod.Post) {
                        val requestBody = request.body.toByteArray().decodeToString()
                        val bodyMap = json.decodeFromString<Map<String, String>>(requestBody)
                        assertEquals("test_dataset", bodyMap["name"])
                        assertEquals("test_space_id", bodyMap["space_id"])
                        assertEquals("0", bodyMap["format_type"])
                        assertEquals("test description", bodyMap["description"])
                        
                        respond(
                            content = """{"code":0,"message":"success","data":{"dataset_id":"test_dataset_id"}}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    } else {
                        assertEquals("test_space_id", request.url.parameters["space_id"])
                        assertEquals("10", request.url.parameters["page_size"])
                        assertEquals("1", request.url.parameters["page_num"])
                        
                        respond(
                            content = """{"code":0,"message":"success","data":{"total_count":1,"dataset_list":[{"dataset_id":"test_dataset_id","name":"test_dataset","description":"test description","space_id":"test_space_id","status":"1","format_type":"0"}]}}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                }
                "/v1/datasets/test_dataset_id" -> {
                    if (request.method == HttpMethod.Put) {
                        val requestBody = request.body.toByteArray().decodeToString()
                        val bodyMap = json.decodeFromString<Map<String, String?>>(requestBody)
                        assertEquals("updated_dataset", bodyMap["name"])
                        assertEquals("updated description", bodyMap["description"])
                        
                        respond(
                            content = """{"code":0,"message":"success"}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    } else {
                        respond(
                            content = """{"code":0,"message":"success"}""",
                            status = HttpStatusCode.OK,
                            headers = headersOf(HttpHeaders.ContentType, "application/json")
                        )
                    }
                }
                "/v1/datasets/test_dataset_id/process" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val bodyMap = json.decodeFromString<Map<String, List<String>>>(requestBody)
                    assertEquals(listOf("test_document_id"), bodyMap["document_ids"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":[{"document_id":"test_document_id","status":"1","progress":100,"update_type":"0"}]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        datasetService = DatasetService()
        datasetService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试创建数据集
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testCreateDataset() = runTest {
        val response = datasetService.create(
            name = "test_dataset",
            spaceId = "test_space_id",
            formatType = DocumentFormatType.DOCUMENT,
            description = "test description"
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertEquals("test_dataset_id", response.data?.datasetId)
        assertTrue(requestsVerified.contains("/v1/datasets"))

        assertFailsWith<IllegalArgumentException> {
            datasetService.create(
                name = "",
                spaceId = "test_space_id",
                formatType = DocumentFormatType.DOCUMENT,
                description = "test description"
            )
        }
    }

    /**
     * 测试更新数据集
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testUpdateDataset() = runTest {
        val response = datasetService.update(
            datasetId = "test_dataset_id",
            name = "updated_dataset",
            description = "updated description"
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertTrue(requestsVerified.contains("/v1/datasets/test_dataset_id"))

        assertFailsWith<IllegalArgumentException> {
            datasetService.update(
                datasetId = "",
                name = "updated_dataset",
                description = "updated description"
            )
        }
    }

    /**
     * 测试删除数据集
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testDeleteDataset() = runTest {
        val response = datasetService.delete("test_dataset_id")
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertTrue(requestsVerified.contains("/v1/datasets/test_dataset_id"))

        assertFailsWith<IllegalArgumentException> {
            datasetService.delete("")
        }
    }

    /**
     * 测试获取数据集列表
     * 
     * 测试点：
     * 1. 验证分页参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testListDatasets() = runTest {
        val response = datasetService.list(
            spaceId = "test_space_id",
            pageSize = 10,
            pageNum = 1
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertEquals(1, response.data?.totalCount)
        assertEquals(1, response.data?.datasetList?.size)
        assertEquals("test_dataset_id", response.data?.datasetList?.get(0)?.datasetId)
        assertTrue(requestsVerified.contains("/v1/datasets"))

        assertFailsWith<IllegalArgumentException> {
            datasetService.list(
                spaceId = "",
                pageSize = 10,
                pageNum = 1
            )
        }
    }

    /**
     * 测试处理数据集文档
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testProcessDataset() = runTest {
        val response = datasetService.process(
            datasetId = "test_dataset_id",
            documentIds = listOf("test_document_id")
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertEquals(1, response.data?.size)
        assertEquals("test_document_id", response.data?.get(0)?.documentId)
        assertTrue(requestsVerified.contains("/v1/datasets/test_dataset_id/process"))

        assertFailsWith<IllegalArgumentException> {
            datasetService.process(
                datasetId = "",
                documentIds = listOf("test_document_id")
            )
        }
    }
} 