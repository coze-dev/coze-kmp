package com.coze.api.auth

import com.coze.api.helper.APIClient
import com.coze.api.model.auth.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.serialization.json.*

val AuthBaseURL = "https://37rtimftq5x63.ahost.marscode.site"

object AuthService {
    private val api = APIClient(baseURL = AuthBaseURL)

    suspend fun getJWTToken(
        appId: String,
        keyId: String,
        privateKey: String
    ): GetTokenData? {
        try {
            // 参数验证
            require(appId.isNotBlank()) { "appId cannot be empty" }
            require(keyId.isNotBlank()) { "keyId cannot be empty" }
            require(privateKey.isNotBlank()) { "privateKey cannot be empty" }

            // 清理私钥
            val cleanKey = try {
                privateKey.trim()
            } catch (e: Exception) {
                throw IllegalArgumentException("Failed to clean private key: ${e.message}")
            }

            // 准备请求体
            val requestBody = GetJWTTokenRequest(
                appId = appId,
                keyId = keyId,
                privateKey = cleanKey
            )

            // 发送请求
            val response = try {
                api.request(
                    method = HttpMethod.Post,
                    path = "/tools/coze/jwt",
                    token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmdW5jdGlvbmlkIjoiMzdydGltZnRxNXg2MyIsImlhdCI6MTcyNTc1NDk1OCwianRpIjoiNjZkY2VlNGU3OWEwMDI3MTEwYjg1YmQ4IiwidHlwZSI6ImZ1bmN0aW9uIiwidXNlcmlkIjoiQ0xPVURJREVfMWRrbnc5NTFuNXA1dm5fNzQxMjA1ODY0NjcwNzE3NjQ2NSIsInZlcnNpb24iOjJ9.H8d6QRxtWk17FAVAKvucWM__oEIL67ow6ENFM22A64E",
                    body = requestBody
                )
            } catch (e: Exception) {
                throw Exception("Failed to send request for JWT token: ${e.message}")
            }

            // 解析响应
            val responseText = response.bodyAsText()
            
            val jwtResponse = try {
                Json.decodeFromString<JWTResponse>(responseText)
            } catch (e: Exception) {
                throw Exception("Failed to parse JWT response: ${e.message}, Response: $responseText")
            }

            // 检查响应状态
            if (jwtResponse.code != 0) {
                throw IllegalStateException("Failed to get JWT token, server returned code ${jwtResponse.code}: ${jwtResponse.data}")
            }
            println("[JWT] Token获取成功, token: ${jwtResponse.data?.accessToken}")
            return jwtResponse.data
            
        } catch (e: Exception) {
            println("[JWT] 错误: ${e.message}")
            throw e
        }
    }
}