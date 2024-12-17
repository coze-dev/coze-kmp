package com.coze.api.helper

import com.coze.api.model.ChatEventType
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.sse.*
import io.ktor.http.ContentType
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.serializer

const val COZE_COM_API_URL = "https://api.coze.com"
const val COZE_COM_BASE_URL = "https://www.coze.com"

data class RequestOptions(
    val headers: Map<String, String>? = null,
    val timeout: Long? = null
)

class APIError(
    val statusCode: Int,
    override val message: String,
    val rawError: RawError? = null,
    cause: Throwable? = null
) : Exception(message, cause) {
    data class RawError(
        val error: String,
        val error_description: String?
    )

    companion object {
        fun generate(
            status: Int,
            rawError: RawError? = null,
            message: String? = null,
            cause: Throwable? = null
        ): APIError {
            return APIError(
                statusCode = status,
                message = message ?: rawError?.error_description ?: "Unknown error",
                rawError = rawError,
                cause = cause
            )
        }
    }
}

open class APIClient(
    private val token: String,
    private val baseURL: String? = null
) {
    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
                ignoreUnknownKeys = true
            })
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.HEADERS
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        install(SSE) {
            showCommentEvents()
            showRetryEvents()
        }
    }
    private val baseUrl = baseURL ?: COZE_COM_API_URL

    suspend fun <T> post(
        path: String,
        payload: Any?,
        serializer: KSerializer<T>,
        useAuth: Boolean = true,
        options: RequestOptions? = null
    ): T {
        val response = httpClient.post("$baseUrl$path") {
            contentType(ContentType.Application.Json)
            if (useAuth) {
                header("Authorization", "Bearer $token")
            }
            options?.headers?.forEach { (key, value) ->
                header(key, value)
            }
            if (payload != null) {
                setBody(payload)
            }
        }

        if (!response.status.isSuccess()) {
            throw APIError(
                statusCode = response.status.value,
                message = response.bodyAsText()
            )
        }

        return Json.decodeFromString(serializer, response.bodyAsText())
    }

    suspend fun <T> get(
        path: String,
        serializer: KSerializer<T>,
        useAuth: Boolean = true,
        options: RequestOptions? = null
    ): T {
        val response = httpClient.get("$baseUrl$path") {
            if (useAuth) {
                header("Authorization", "Bearer $token")
            }
            options?.headers?.forEach { (key, value) ->
                header(key, value)
            }
        }

        if (!response.status.isSuccess()) {
            throw APIError(
                statusCode = response.status.value,
                message = response.bodyAsText()
            )
        }

        return Json.decodeFromString(serializer, response.bodyAsText())
    }

    suspend fun sse(
        path: String,
        body: Any? = null,
        useAuth: Boolean = true,
        options: RequestOptions? = null
    ): Flow<ServerSentEvent> = flow {
        httpClient.sse("$baseUrl$path", {
            method = HttpMethod.Post
            if (useAuth) {
                header("Authorization", "Bearer $token")
            }
            header("Accept", "text/event-stream")
            header("Cache-Control", "no-cache")
            header("Connection", "keep-alive")
            options?.headers?.forEach { (key, value) ->
                header(key, value)
            }
            contentType(ContentType.Application.Json)
            body?.let { setBody(it) }
        }) {
            var shouldContinue = true
            while (shouldContinue) {
                incoming.collect { event ->
                    val eventType = event.event?.let { ChatEventType.fromValue(it) }
                    emit(event)
                    if (eventType == ChatEventType.DONE) {
                        shouldContinue = false
                        return@collect
                    }
                }
            }
        }
    }
}