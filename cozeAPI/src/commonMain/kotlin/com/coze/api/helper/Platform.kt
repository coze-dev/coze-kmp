package com.coze.api.helper

/**
 * Generate code verifier for OAuth | 生成OAuth的代码验证器
 * @return String Code verifier | 代码验证器
 */
expect fun generateCodeVerifier(): String

/**
 * Generate code challenge from verifier | 从验证器生成代码挑战
 * @param codeVerifier Code verifier | 代码验证器
 * @return String Code challenge | 代码挑战
 */
expect fun generateCodeChallenge(codeVerifier: String): String

/**
 * Check if running in browser | 检查是否在浏览器中运行
 * @return Boolean True if in browser | 如果在浏览器中返回true
 */
expect fun isBrowser(): Boolean

/**
 * Platform interface | 平台接口
 * Defines platform-specific functionality | 定义平台特定的功能
 */
interface Platform {
    /**
     * Platform name | 平台名称
     */
    val name: String
}

/**
 * Get current platform | 获取当前平台
 * @return Platform Current platform instance | 当前平台实例
 */
expect fun getPlatform(): Platform