package com.coze.api.model.auth

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class JWTToken(
    val access_token: String,
    val expires_in: Int,
    @SerialName("token_type") val tokenType: String? = null
)

@Serializable
data class JWTScope(
    val account_permission: AccountPermission,
    val attribute_constraint: AttributeConstraint
) {
    @Serializable
    data class AccountPermission(
        val permission_list: List<String>
    )

    @Serializable
    data class AttributeConstraint(
        val connector_bot_chat_attribute: ConnectorBotChatAttribute
    ) {
        @Serializable
        data class ConnectorBotChatAttribute(
            val bot_id_list: List<String>
        )
    }
}

data class JWTTokenConfig(
    val baseURL: String? = null,
    val durationSeconds: Int? = null,
    val appId: String,
    val aud: String,
    val keyId: String,
    val privateKey: String,
    val algorithm: String? = null,
    val scope: JWTScope? = null,
    val sessionName: String? = null
)
