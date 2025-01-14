package com.coze.demo

import com.coze.api.helper.TokenManager
import com.coze.demo.auth.AuthService

/**
 * Authentication Demo | 认证演示
 * Demonstrates authentication functionality | 演示认证功能
 */
object AuthDemo {
    init {
        // Initialize TokenManager with AuthService | 使用 AuthService 初始化 TokenManager
        TokenManager.init(AuthService)
    }

    /**
     * Get JWT authentication token | 获取JWT认证令牌
     * @return String? JWT access token if successful | 成功时返回JWT访问令牌
     */
    suspend fun getJWTAuth(): String? {
        return try {
            TokenManager.getToken()
        } catch (e: Exception) {
            null
        }
    }
}
