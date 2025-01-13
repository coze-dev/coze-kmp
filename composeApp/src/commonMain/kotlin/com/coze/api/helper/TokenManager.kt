package com.coze.api.helper

import com.coze.api.auth.AuthService
import kotlinx.datetime.Clock

object TokenManager {
    private var _token: String? = null
    private var tokenExpireTime: Long = 0

    suspend fun getTokenAsync(forceRefresh: Boolean = false): String {
        val now = Clock.System.now().epochSeconds
        
        // 如果token不存在或已过期（提前30秒认为过期），重新获取
        if (_token == null || now >= tokenExpireTime - 30 || forceRefresh) {
            val (token, expireIn) = generateToken()
            _token = token
            // 根据返回的过期时间设置
            tokenExpireTime = now + expireIn
        }
        return _token ?: throw IllegalStateException("Token not available")
    }

    private suspend fun generateToken(): Pair<String?, Long> {
        try {
            val jwtRsp = AuthService.getJWTToken(
                API_CONFIG.APP_ID, 
                API_CONFIG.KEY_ID, 
                API_CONFIG.PRIVATE_KEY
                    .trimIndent()
                    .lines()
                    .joinToString("\n")
                    .trim()
            )
            println("JWT response: $jwtRsp")

            val token = jwtRsp?.accessToken
            if (token?.isEmpty() == true) {
                throw IllegalStateException("Received empty access token from server")
            }
            // 返回token和过期时间
            return Pair(token, jwtRsp?.expiresIn ?: 900L)
        } catch (e: Exception) {
            println("Token generation failed: ${e.message}")
            throw IllegalStateException("Failed to generate token: ${e.message}", e)
        }
    }

    private object API_CONFIG {
        const val APP_ID = "xxx"
        const val KEY_ID = "xxx"
        const val PRIVATE_KEY = """
-----BEGIN PRIVATE KEY-----
xxx
-----END PRIVATE KEY-----
        """
    }
}