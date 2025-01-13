package com.coze.api.auth

import com.coze.api.helper.APIClient
import com.coze.api.model.auth.GetJWTTokenRequest
import com.coze.api.model.auth.JWTResponse
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import kotlin.test.*
import kotlinx.coroutines.test.runTest

/**
 * 认证服务测试类
 * 
 * 测试范围：
 * 1. 获取 JWT Token
 * 2. 错误处理
 * 3. 网络错误处理
 * 
 * 测试策略：
 * 1. 使用 MockEngine 模拟 HTTP 响应
 * 2. 验证请求参数的正确性
 * 3. 验证响应数据的解析
 * 4. 验证错误处理逻辑
 */
class AuthServiceTest {
    /**
     * 测试获取 JWT Token
     * 
     * 测试点：
     * 1. 验证请求路径和方法
     * 2. 验证响应数据解析
     */
    @Test
    fun testGetJWTToken() = runTest {
        val mockEngine = MockEngine { request ->
            assertEquals("/tools/coze/jwt", request.url.encodedPath)
            assertEquals(HttpMethod.Post, request.method)
            
            respond(
                content = ByteReadChannel("""
                    {
                        "code": 0,
                        "data": {
                            "access_token": "test.access.token",
                            "token_type": "Bearer",
                            "expires_in": 3600
                        },
                        "message": "success"
                    }
                """.trimIndent()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        
        val client = APIClient(
            baseURL = AuthBaseURL,
            client = mockClient
        )
        
        val result = client.post<JWTResponse>("/tools/coze/jwt", GetJWTTokenRequest(
            appId = "test-app-id",
            keyId = "test-key-id",
            privateKey = "test-private-key"
        ))
        
        assertNotNull(result)
        assertEquals(0, result.code)
        assertNotNull(result.data)
        assertEquals("test.access.token", result.data?.accessToken)
        assertEquals("Bearer", result.data?.tokenType)
        assertEquals(3600L, result.data?.expiresIn)
    }
    
    /**
     * 测试获取 JWT Token 时的错误响应
     * 
     * 测试点：
     * 1. 验证错误响应的解析
     * 2. 验证错误码和消息
     */
    @Test
    fun testGetJWTTokenError() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = ByteReadChannel("""
                    {
                        "code": 1,
                        "message": "Invalid credentials",
                        "data": null
                    }
                """.trimIndent()),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        
        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        
        val client = APIClient(
            baseURL = AuthBaseURL,
            client = mockClient
        )
        
        val result = client.post<JWTResponse>("/tools/coze/jwt", GetJWTTokenRequest(
            appId = "test-app-id",
            keyId = "test-key-id",
            privateKey = "test-private-key"
        ))
        
        assertNotNull(result)
        assertEquals(1, result.code)
        assertNull(result.data)
        assertEquals("Invalid credentials", result.message)
    }
    
    /**
     * 测试获取 JWT Token 时的网络错误
     * 
     * 测试点：
     * 1. 验证网络错误的异常处理
     */
    @Test
    fun testGetJWTTokenNetworkError() = runTest {
        val mockEngine = MockEngine { _ ->
            respondError(HttpStatusCode.ServiceUnavailable)
        }
        
        val mockClient = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        
        val client = APIClient(
            baseURL = AuthBaseURL,
            client = mockClient
        )
        
        assertFailsWith<Exception> {
            client.post<JWTResponse>("/tools/coze/jwt", GetJWTTokenRequest(
                appId = "test-app-id",
                keyId = "test-key-id",
                privateKey = "test-private-key"
            ))
        }
    }
}