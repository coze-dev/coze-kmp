package com.coze.demo

import com.coze.api.helper.TokenManager

/**
 * Demo object for authentication functionality
 */
object AuthDemo {
    /**
     * Test JWT authentication
     * @return JWT access token if successful, null otherwise
     */
    suspend fun getJWTAuth(): String? {
        return try {
            val token = TokenManager.getTokenAsync()
            println("[AuthDemo] Successfully obtained token $token")
            token
        } catch (e: Exception) {
            println("[AuthDemo] JWT authentication failed: ${e.message}")
            null
        }
    }
}
