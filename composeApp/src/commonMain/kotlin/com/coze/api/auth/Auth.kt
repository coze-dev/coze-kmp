package com.coze.api.auth

import com.coze.api.helper.APIClient
import com.coze.api.helper.APIError
import com.coze.api.helper.RequestOptions
import com.coze.api.helper.getJWTProvider
import com.coze.api.helper.isBrowser
import kotlinx.datetime.Clock
import com.coze.api.model.auth.*
import kotlinx.serialization.serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// 主要功能函数
object Auth {
    private suspend fun _getJWTToken(
        config: Map<String, Any>,
        options: RequestOptions? = null
    ): JWTToken {
        val api = APIClient(token = config["token"] as String, baseURL = config["baseURL"] as? String)
        // println("config: $config")

        val payload = buildJsonObject {
            put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            put("duration_seconds", (config["durationSeconds"] as? Int ?: 900).toInt())
            if (config["scope"] != null) {
                put("scope", config["scope"].toString())
            }
        }
        // println("payload: $payload")

        val jsonPayload = Json.encodeToString(JsonObject.serializer(), payload)

        return api.post(
            path = "/api/permission/oauth2/token",
            payload = jsonPayload,
            serializer = serializer<JWTToken>(),
            useAuth = true,
            options = options
        )
    }

    suspend fun getJWTToken(
        config: JWTTokenConfig,
        options: RequestOptions? = null
    ): JWTToken {
        if (isBrowser()) {
            throw Exception("getJWTToken is not supported in browser")
        }

        // Trim private key and validate format
        val trimmedPrivateKey = config.privateKey.trim()
        val keyFormat = when {
            trimmedPrivateKey.contains("BEGIN RSA PRIVATE KEY") -> "RSA"
            trimmedPrivateKey.contains("BEGIN PRIVATE KEY") -> "PKCS8"
            else -> null
        }

        if (keyFormat == null) {
            throw APIError(
                400,
                "Invalid private key format. Expected PEM format (RSA or PKCS8)"
            )
        }

        // 准备JWT payload
        val now = Clock.System.now().epochSeconds
        val jwtPayload = buildJsonObject {
            put("iss", config.appId)
            put("aud", config.aud)
            put("iat", now)
            put("exp", now + 3600) // 1小时
            put("jti", now.toString(16))
            if (config.sessionName != null) {
                put("session_name", config.sessionName)
            }
        }
        println("jwtPayload: $jwtPayload")

        // 将JsonObject转换为Map
        val jwtPayloadMap = jwtPayload.toMap()

        // 使用JWT provider签名获取token
        val token = getJWTProvider().sign(
            payload = jwtPayloadMap,
            privateKey = trimmedPrivateKey,
            algorithm = config.algorithm ?: "RS256",
            keyid = config.keyId
        )

        // 交换JWT token获取OAuth token
        val tokenConfig = buildMap<String, Any> {
            put("token", token)
            config.baseURL?.let { put("baseURL", it) }
            put("durationSeconds", config.durationSeconds ?: 900)
            config.scope?.let { put("scope", it) }
        }

        return _getJWTToken(tokenConfig, options)
    }

    private fun JsonObject.toMap(): Map<String, Any> {
        return entries.associate { (key, element) ->
            key to when (element) {
                is kotlinx.serialization.json.JsonPrimitive -> {
                    when {
                        element.isString -> element.content
                        element.content.toLongOrNull() != null -> element.content.toLong()
                        element.content.toDoubleOrNull() != null -> element.content.toDouble()
                        element.content == "true" -> true
                        element.content == "false" -> false
                        else -> element.content
                    }
                }
                else -> element.toString()
            }
        }
    }
}
