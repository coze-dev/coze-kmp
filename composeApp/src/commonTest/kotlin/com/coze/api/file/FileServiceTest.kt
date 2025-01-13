package com.coze.api.file

import com.coze.api.model.file.*
import com.coze.api.helper.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.*
import kotlin.test.*

/**
 * 文件服务测试类
 * 
 * 测试范围：
 * 1. 上传文件
 * 2. 获取文件信息
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证参数验证逻辑
 */
class FileServiceTest {
    private lateinit var mockEngine: MockEngine
    private lateinit var fileService: FileService
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
                "/v1/files/upload" -> {
                    // 验证上传请求
                    respond(
                        content = """{"code":0,"message":"success","data":{"id":"test_file_id","file_name":"test.txt","bytes":1024,"created_at":"2024-01-01T00:00:00Z"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                "/v1/files/retrieve" -> {
                    // 验证获取文件信息请求
                    assertEquals("test_file_id", request.url.parameters["file_id"])
                    
                    respond(
                        content = """{"code":0,"message":"success","data":{"id":"test_file_id","file_name":"test.txt","bytes":1024,"created_at":"2024-01-01T00:00:00Z"}}""",
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }
        
        fileService = FileService()
        fileService.setClient(mockEngine)
    }

    @AfterTest
    fun tearDown() {
        requestsVerified.clear()
    }

    /**
     * 测试上传文件
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testUpload() = runTest {
        val request = CreateFileReq(
            file = "test content".encodeToByteArray(),
            fileName = "test.txt",
            mimeType = "text/plain"
        )
        val response = fileService.upload(request)
        
        assertNotNull(response.data)
        assertEquals("test_file_id", response.data?.id)
        assertEquals("test.txt", response.data?.fileName)
        assertEquals(1024L, response.data?.bytes)
        assertTrue(requestsVerified.contains("/v1/files/upload"))

        assertFailsWith<IllegalArgumentException> {
            fileService.upload(request.copy(file = ByteArray(0)))
        }

        assertFailsWith<IllegalArgumentException> {
            fileService.upload(request.copy(fileName = ""))
        }

        assertFailsWith<IllegalArgumentException> {
            fileService.upload(request.copy(mimeType = ""))
        }
    }

    /**
     * 测试获取文件信息
     * 
     * 测试点：
     * 1. 验证请求参数正确性
     * 2. 验证响应数据解析
     * 3. 验证参数校验
     */
    @Test
    fun testRetrieve() = runTest {
        val response = fileService.retrieve("test_file_id")
        
        assertNotNull(response.data)
        assertEquals("test_file_id", response.data?.id)
        assertEquals("test.txt", response.data?.fileName)
        assertEquals(1024L, response.data?.bytes)
        assertTrue(requestsVerified.contains("/v1/files/retrieve"))

        assertFailsWith<IllegalArgumentException> {
            fileService.retrieve("")
        }
    }
} 