package com.coze.api.dataset

import com.coze.api.model.dataset.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 文档服务测试类
 * 
 * 测试范围：
 * 1. 创建文档
 * 2. 更新文档
 * 3. 删除文档
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证参数验证逻辑
 */
class DocumentServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var documentService: DocumentService
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
                "/open_api/knowledge/document/create" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val bodyMap = json.decodeFromString<CreateDocumentRequest>(requestBody)
                    assertEquals("test_dataset_id", bodyMap.datasetId)
                    assertEquals(1, bodyMap.documentBases.size)
                    assertEquals("test_document", bodyMap.documentBases[0].name)
                    val sourceInfo = bodyMap.documentBases[0].sourceInfo
                    assertNotNull(sourceInfo.fileBase64)
                    assertEquals(DocumentFileType.TXT, sourceInfo.fileType)
                    assertEquals(DocumentSourceType.LOCAL_FILE.value, sourceInfo.documentSource)
                    
                    respond(
                        content = """{"code":0,"message":"success","document_infos":[{"document_id":"test_document_id","name":"test_document","char_count":100,"slice_count":1,"hit_count":0,"size":1024,"type":"txt","source_type":0,"status":1,"format_type":0,"update_type":0,"update_interval":24,"create_time":1704067200,"update_time":1704067200}]}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/open_api/knowledge/document/update" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val bodyMap = json.decodeFromString<UpdateDocumentRequest>(requestBody)
                    assertEquals("test_document_id", bodyMap.documentId)
                    assertEquals("updated_document", bodyMap.documentName)
                    
                    respond(
                        content = """{"code":0,"message":"success"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/open_api/knowledge/document/delete" -> {
                    val requestBody = request.body.toByteArray().decodeToString()
                    val bodyMap = json.decodeFromString<DeleteDocumentRequest>(requestBody)
                    assertEquals(listOf("test_document_id"), bodyMap.documentIds)
                    
                    respond(
                        content = """{"code":0,"message":"success"}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        documentService = DocumentService()
        documentService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试创建文档
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testCreateDocument() = runTest {
        val response = documentService.create(
            datasetId = "test_dataset_id",
            documentBases = listOf(
                DocumentBase(
                    name = "test_document",
                    sourceInfo = DocumentSourceInfo.buildLocalFile("test content")
                )
            )
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertNotNull(response.data)
        assertEquals(1, response.data?.size)
        assertEquals("test_document_id", response.data?.get(0)?.documentId)
        assertTrue(requestsVerified.contains("/open_api/knowledge/document/create"))

        assertFailsWith<IllegalArgumentException> {
            documentService.create(
                datasetId = "test_dataset_id",
                documentBases = emptyList()
            )
        }
    }

    /**
     * 测试更新文档
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testUpdateDocument() = runTest {
        val response = documentService.update(
            documentId = "test_document_id",
            documentName = "updated_document"
        )
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertTrue(requestsVerified.contains("/open_api/knowledge/document/update"))

        assertFailsWith<IllegalArgumentException> {
            documentService.update(
                documentId = "",
                documentName = "updated_document"
            )
        }
    }

    /**
     * 测试删除文档
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testDeleteDocument() = runTest {
        val response = documentService.delete(listOf("test_document_id"))
        
        assertNotNull(response)
        assertEquals(0, response.code)
        assertTrue(requestsVerified.contains("/open_api/knowledge/document/delete"))

        assertFailsWith<IllegalArgumentException> {
            documentService.delete(emptyList())
        }
    }
}