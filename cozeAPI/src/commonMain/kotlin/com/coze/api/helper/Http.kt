// @file:OptIn(kotlinx.serialization.ExperimentalSerializationApi::class)

package com.coze.api.helper

import com.coze.api.model.ApiResponse
import com.coze.api.model.AuthenticationError
import com.coze.api.model.EventType
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.sse.*
import io.ktor.client.request.*
import io.ktor.client.request.forms.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

const val API_URL = "https://api.coze.com"

expect fun createPlatformEngine(): HttpClientEngine

data class RequestOptions(
    val headers: Map<String, String> = emptyMap(),
    val params: Map<String, String> = emptyMap()
)

open class APIClient(
    private val baseURL: String? = API_URL,
    val token: String? = null,
    private val client: HttpClient? = null,
) {
    val jsonUtil = Json {
        prettyPrint = true
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        coerceInputValues = true
    }
    
    private val defaultClient by lazy {
        HttpClient(createPlatformEngine()) {
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
    }

    var httpClient: HttpClient = client ?: defaultClient
        private set

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
            val response = httpClient.request(url) {
                this.method = method
                headers {
                    val actualToken = token ?: this@APIClient.token
                    actualToken?.let { append(HttpHeaders.Authorization, "Bearer ${it}") }
                    options?.headers?.forEach { (key, value) -> append(key, value) }
                }
                if (method != HttpMethod.Get && body != null) {
                    when (body) {
                        is MultiPartFormDataContent -> {
                            contentType(ContentType.MultiPart.FormData)
                            setBody(body)
                        }
                        is String -> {
                            contentType(ContentType.Text.Plain)
                            setBody(body)
                        }
                        is JsonObject -> {
                            contentType(ContentType.Application.Json)
                            setBody(jsonUtil.encodeToString(JsonObject.serializer(), body))
                        }
                        else -> {
                            contentType(ContentType.Application.Json)
                            setBody(body)
                        }
                    }
                }
            }
            
            if (response.status != HttpStatusCode.OK) {
                println("[HTTP] Error response: ${response.status} - ${response.bodyAsText()}")
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

    suspend inline fun <reified T> put(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): T {
        println("[HTTP] Putting to $path with payload $payload")
        val response = request(HttpMethod.Put, path, null, payload, options)
        val responseText = response.bodyAsText()
        return jsonUtil.decodeFromString(serializer<T>(), responseText)
    }

    suspend inline fun <reified T> delete(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): T {
        println("[HTTP] Deleting from $path with payload $payload")
        val response = request(HttpMethod.Delete, path, null, payload, options)
        val responseText = response.bodyAsText()
        return jsonUtil.decodeFromString(serializer<T>(), responseText)
    }

    fun sse(
        path: String,
        body: Any? = null,
        options: RequestOptions? = null
    ): Flow<ServerSentEvent> = flow {
        println("[HTTP] SSE posting to $path with payload $body")
        httpClient.sse(baseURL + path, {
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
            body?.let { 
                setBody(it) 
            }
        }) {
            var shouldContinue = true
            var eventCount = 0
            val maxEvents = 500 // 最大事件数限制
            
            while (shouldContinue && eventCount < maxEvents) {
                try {
                    incoming.collect { event ->
                        if (!isActive) {
                            println("[HTTP] SSE connection is no longer active")
                            shouldContinue = false
                            return@collect
                        }
                        
                        eventCount++
                        println("[HTTP] SSE event ($eventCount/$maxEvents): $event")
                        val eventType = event.event?.let { EventType.fromValue(it) }
                        // 如果 event.event 是 EventType.ERROR，则打印 msg 然后结束
                        if (eventType == EventType.ERROR) {
                            shouldContinue = false
                            return@collect
                        }
                        emit(event)
                        if (eventType == EventType.DONE || eventType == EventType.WORKFLOW_DONE) {
                            shouldContinue = false
                            return@collect
                        }
                    }
                } catch (e: Exception) {
                    println("[HTTP] SSE connection error: ${e.message}")
                    shouldContinue = false
                    throw e
                }
            }
            
            if (eventCount >= maxEvents) {
                println("[HTTP] SSE reached maximum event limit ($maxEvents)")
                throw Exception("SSE reached maximum event limit")
            }
        }
    }

    fun createHttpClient(engine: HttpClientEngine): HttpClient {
        return HttpClient(engine) {
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
    }
}

open class APIBase(
    protected val baseURL: String = API_URL,
) {
    private var httpClient: HttpClient? = null
    private var apiClient: APIClient? = null

    internal fun setHttpClient(client: HttpClient) {
        httpClient = client
        apiClient = APIClient(baseURL, null, client)
    }

    internal fun setClient(engine: HttpClientEngine) {
        val client = HttpClient(engine) {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                    explicitNulls = false
                    coerceInputValues = true
                })
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
                sanitizeHeader { header -> header == HttpHeaders.Authorization }
            }
            install(SSE)
        }
        setHttpClient(client)
    }

    protected suspend fun getClient(): APIClient {
        if (httpClient != null && apiClient != null) {
            return apiClient!!
        }
        
        var retryCount = 0
        val maxRetries = 1
        
        while (true) {
            println("[HTTP] Getting client with token")
            try {
                val token = TokenManager.getTokenAsync(true)
                apiClient = APIClient(baseURL, token, httpClient)
                return apiClient!!
            } catch (e: Exception) {
                if (e is AuthenticationError && retryCount < maxRetries) {
                    println("[HTTP] Token fetch failed (attempt ${retryCount + 1}): ${e.message}")
                    retryCount++
                    continue
                }
                throw e
            }
        }
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
        val response = getClient().post<ApiResponse<T>>(path, payload, options)
        return response
    }

    protected suspend inline fun <reified T> put(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): ApiResponse<T> {
        return getClient().put(path, payload, options)
    }

    protected suspend inline fun <reified T> _delete(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): ApiResponse<T> {
        return getClient().delete(path, payload, options)
    }

    protected suspend fun sse(
        path: String,
        body: Any? = null,
        options: RequestOptions? = null
    ): Flow<ServerSentEvent> {
        return getClient().sse(path, body, options)
    }
} 