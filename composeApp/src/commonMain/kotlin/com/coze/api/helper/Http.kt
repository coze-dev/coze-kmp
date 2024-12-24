package com.coze.api.helper

import com.coze.api.model.ApiResponse
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
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

const val API_URL = "https://api.coze.com"

data class RequestOptions(
    val headers: Map<String, String> = emptyMap(),
    val params: Map<String, String> = emptyMap()
)

open class APIClient(private val baseURL: String? = API_URL, val token: String? = null) {
    val jsonUtil = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
    }

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(jsonUtil)
        }
        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.INFO
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
        install(SSE)
    }

    suspend fun request(
        method: HttpMethod,
        path: String,
        token: String? = null,
        body: Any? = null,
        options: RequestOptions? = null
    ): HttpResponse {
        val queryString = options?.params?.let { params ->
            if (params.isNotEmpty()) {
                params.entries.joinToString("&") { "${it.key}=${it.value}" }
            } else ""
        } ?: ""
        
        val fullPath = if (path.contains("?")) {
            "$path&$queryString"
        } else if (queryString.isNotEmpty()) {
            "$path?$queryString"
        } else path

        val url = (baseURL ?: API_URL) + fullPath
        println("[HTTP] Request URL: $url")

        return try {
            val response = client.request(url) {
                this.method = method
                headers {
                    val actualToken = token ?: this@APIClient.token
                    actualToken?.let { append(HttpHeaders.Authorization, "Bearer ${it}") }
                    options?.headers?.forEach { (key, value) -> append(key, value) }
                }
                if (method != HttpMethod.Get && body != null) {
                    contentType(ContentType.Application.Json)
                    when (body) {
                        is String -> setBody(body)
                        is JsonObject -> setBody(jsonUtil.encodeToString(JsonObject.serializer(), body))
                        else -> setBody(body)
                    }
                }
            }
            
            if (response.status != HttpStatusCode.OK) {
                println("[HTTP] Error response: ${response.bodyAsText()}")
            }
            
            response
        } catch (e: Exception) {
            println("[HTTP] Request failed: ${e.message}")
            throw e
        }
    }

    suspend inline fun <reified T> get(
        path: String,
        options: RequestOptions? = null
    ): T {
        val opts = if (options?.params != null) {
            RequestOptions(
                params = options.params + options.params,
                headers = options.headers
            )
        } else options

        val response = request(HttpMethod.Get, path, null, options = opts)
        val responseText = response.bodyAsText()
        return jsonUtil.decodeFromString(serializer<T>(), responseText)
    }

    suspend inline fun <reified T> post(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): T {
        println("[HTTP] Posting to $path with payload $payload")
        val response = request(HttpMethod.Post, path, null, payload, options)
        val responseText = response.bodyAsText()
        return jsonUtil.decodeFromString(serializer<T>(), responseText)
    }

    fun sse(
        path: String,
        body: Any? = null,
        options: RequestOptions? = null
    ): Flow<ServerSentEvent> = flow {
        client.sse(baseURL + path, {
            method = HttpMethod.Post
            token?.let { 
                header("Authorization", "Bearer $it")
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

open class APIBase(
    protected val baseURL: String = API_URL,
) {
    protected suspend fun getClient(): APIClient {
        return APIClient(baseURL, TokenManager.getTokenAsync())
    }

    protected suspend inline fun <reified T> get(
        path: String,
        options: RequestOptions? = null
    ): ApiResponse<T> {
        return getClient().get(path, options)
    }

    protected suspend inline fun <reified T> post(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): ApiResponse<T> {
        return getClient().post(path, payload, options)
    }

    protected suspend fun sse(
        path: String,
        body: Any? = null,
        options: RequestOptions? = null
    ): Flow<ServerSentEvent> {
        return getClient().sse(path, body, options)
    }
} 
