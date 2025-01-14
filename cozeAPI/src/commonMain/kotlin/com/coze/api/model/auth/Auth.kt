package com.coze.api.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


/**
 * Token Information | Token信息
 * @property token Access token | 访问令牌
 * @property expiresIn Expiration time in seconds | 过期时间（秒）
 */
data class TokenInfo(
    val token: String?,
    val expiresIn: Long
) 

@Serializable
data class GetTokenReq(
    @SerialName("app_id")
    val appId: String,
    @SerialName("app_secret")
    val appSecret: String,
    @SerialName("duration_seconds")
    val durationSeconds: Int? = null
)

@Serializable
data class GetTokenData(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long
)

@Serializable
data class RefreshTokenReq(
    @SerialName("refresh_token")
    val refreshToken: String,
    @SerialName("duration_seconds")
    val durationSeconds: Int? = null
)

@Serializable
data class RevokeTokenReq(
    @SerialName("access_token")
    val accessToken: String
)

@Serializable
data class JWTToken(
    @SerialName("access_token")
    val accessToken: String,
    @SerialName("token_type")
    val tokenType: String,
    @SerialName("expires_in")
    val expiresIn: Long
)

@Serializable
data class JWTTokenConfig(
    val appId: String,
    val privateKey: String,
    val aud: String = "api.coze.com",
    val algorithm: String? = "RS256",
    val keyId: String,
    val sessionName: String? = null,
    val baseURL: String? = null,
    val durationSeconds: Int? = 900,
    val scope: String? = null
)

@Serializable
data class JWTResponse(
    val code: Int,
    val data: GetTokenData? = null,
    val message: String? = null
)

@Serializable
data class GetJWTTokenRequest(
    val appId: String,
    val keyId: String,
    val privateKey: String
)
