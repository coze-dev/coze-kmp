package com.coze.api.helper

import com.coze.api.model.auth.TokenInfo
import kotlinx.datetime.Clock

/**
 * Token Service Interface | Token服务接口
 * Interface for token generation services | Token生成服务的接口定义
 */
interface TokenService {
    /**
     * Get token from service | 从服务获取Token
     * @return TokenInfo Token information including expiration time | Token信息，包含过期时间
     */
    suspend fun getToken(): TokenInfo
}

/**
 * Token Manager | Token管理器
 * Handles token caching and refresh | 处理Token的缓存和刷新
 */
object TokenManager {
    private var _token: String? = null
    private var tokenExpireTime: Long = 0
    private lateinit var tokenService: TokenService

    /**
     * Initialize with token service | 使用Token服务初始化
     * @param service Token service implementation | Token服务实现
     */
    fun init(service: TokenService) {
        tokenService = service
    }

    /**
     * Get token | 获取Token
     * @param forceRefresh Force token refresh | 强制刷新Token
     * @return String Valid token | 有效的Token
     */
    suspend fun getToken(forceRefresh: Boolean = false): String {
        val now = Clock.System.now().epochSeconds
        
        // Refresh token if null, expired (30s before), or forced | 如果token不存在、即将过期（提前30秒）或强制刷新，则重新获取
        if (_token == null || now >= tokenExpireTime - 30 || forceRefresh) {
            val tokenInfo = tokenService.getToken()
            _token = tokenInfo.token
            tokenExpireTime = now + tokenInfo.expiresIn
        }
        return _token ?: throw IllegalStateException("Token not available")
    }
}