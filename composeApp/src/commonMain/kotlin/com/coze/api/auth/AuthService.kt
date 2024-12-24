package com.coze.api.auth

import com.coze.api.helper.APIClient
import com.coze.api.helper.RequestOptions
import com.coze.api.helper.getJWTProvider
import com.coze.api.helper.isBrowser
import com.coze.api.model.APIError
import kotlinx.datetime.Clock
import com.coze.api.model.auth.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer

// 主要功能函数
object AuthService {
    private suspend fun _getJWTToken(
        config: Map<String, Any>,
        options: RequestOptions? = null
    ): JWTToken {
        val api = APIClient(token = config["token"] as String, baseURL = config["baseURL"] as? String)

        val payload = buildJsonObject {
            put("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer")
            put("duration_seconds", (config["durationSeconds"] as? Int ?: 900).toInt())
            if (config["scope"] != null) {
                put("scope", config["scope"].toString())
            }
        }

        val jsonPayload = Json.encodeToString(JsonObject.serializer(), payload)

        // println("_getJWTToken post payload: $jsonPayload, token: ${config["token"]}")
        val response = api.request(HttpMethod.Post, "/api/permission/oauth2/token", config["token"] as String, payload, options)
        return Json.decodeFromString(serializer<JWTToken>(), response.bodyAsText())
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
                null,
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