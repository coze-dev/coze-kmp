package com.coze.demo

import com.coze.api.auth.Auth
import com.coze.api.model.auth.*
import kotlinx.coroutines.runBlocking

object AuthDemo {
    // 测试配置
    private const val APP_ID = "xxx"
    private const val AUD = "api.coze.com"
    private const val KEY_ID = "xxx"
    private const val PRIVATE_KEY = """
        -----BEGIN PRIVATE KEY-----
        xxx
        -----END PRIVATE KEY-----
    """

    fun testJWTAuth(): String? {
        return runBlocking {
            val config = JWTTokenConfig(
                appId = APP_ID,
                aud = AUD,
                keyId = KEY_ID,
                privateKey = PRIVATE_KEY,
                // scope = JWTScope(
                //     account_permission = JWTScope.AccountPermission(
                //         permission_list = listOf("chat")
                //     ),
                //     attribute_constraint = JWTScope.AttributeConstraint(
                //         connector_bot_chat_attribute = JWTScope.AttributeConstraint.ConnectorBotChatAttribute(
                //             bot_id_list = listOf("bot_id_1", "bot_id_2")
                //         )
                //     )
                // )
            )
            try {
                val token = Auth.getJWTToken(config)
                println("JWT Access Token: ${token.access_token}")
                println("Expires in: ${token.expires_in}")
                return@runBlocking token.access_token
            } catch (e: Exception) {
                println("Error getting JWT token: ${e.message}")
                return@runBlocking null
            }
        }
    }

}
