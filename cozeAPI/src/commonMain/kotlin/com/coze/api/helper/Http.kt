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

/**
 * Create platform-specific HTTP client engine | 创建平台特定的HTTP客户端引擎
 */
expect fun createPlatformEngine(): HttpClientEngine

/**
 * Request options for HTTP calls | HTTP请求选项
 * @property headers Custom headers | 自定义请求头
 * @property params Query parameters | 查询参数
 */
data class RequestOptions(
    val headers: Map<String, String> = emptyMap(),
    val params: Map<String, String> = emptyMap()
)

/**
 * API Client for making HTTP requests | HTTP请求的API客户端
 * @property baseURL Base URL for API calls | API调用的基础URL
 * @property token Authentication token | 认证令牌
 * @property client Custom HTTP client | 自定义HTTP客户端
 */
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

    /**
     * Make HTTP request | 发送HTTP请求
     * @param method HTTP method | HTTP方法
     * @param path Request path | 请求路径
     * @param token Authentication token | 认证令牌
     * @param body Request body | 请求体
     * @param options Request options | 请求选项
     */
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
            throw e
        }
    }

    /**
     * Make GET request | 发送GET请求
     * @param path Request path | 请求路径
     * @param options Request options | 请求选项
     */
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

    /**
     * Make POST request | 发送POST请求
     * @param path Request path | 请求路径
     * @param payload Request payload | 请求负载
     * @param options Request options | 请求选项
     */
    suspend inline fun <reified T> post(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): T {
        val response = request(HttpMethod.Post, path, null, payload, options)
        val responseText = response.bodyAsText()
        return jsonUtil.decodeFromString(serializer<T>(), responseText)
    }

    /**
     * Make PUT request | 发送PUT请求
     * @param path Request path | 请求路径
     * @param payload Request payload | 请求负载
     * @param options Request options | 请求选项
     */
    suspend inline fun <reified T> put(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): T {
        val response = request(HttpMethod.Put, path, null, payload, options)
        val responseText = response.bodyAsText()
        return jsonUtil.decodeFromString(serializer<T>(), responseText)
    }

    /**
     * Make DELETE request | 发送DELETE请求
     * @param path Request path | 请求路径
     * @param payload Request payload | 请求负载
     * @param options Request options | 请求选项
     */
    suspend inline fun <reified T> delete(
        path: String,
        payload: Any? = null,
        options: RequestOptions? = null
    ): T {
        val response = request(HttpMethod.Delete, path, null, payload, options)
        val responseText = response.bodyAsText()
        return jsonUtil.decodeFromString(serializer<T>(), responseText)
    }

    /**
     * Create Server-Sent Events connection | 创建服务器发送事件连接
     * @param path Request path | 请求路径
     * @param body Request body | 请求体
     * @param options Request options | 请求选项
     */
    fun sse(
        path: String,
        body: Any? = null,
        options: RequestOptions? = null
    ): Flow<ServerSentEvent> = flow {
        httpClient.sse(baseURL + path, {
            method = HttpMethod.Post
            token?.let { 
                header("Authorization", "Bearer $it")
            }
            header("Accept", "text/event-stream")
            header("Cache-Control", "no-cache")
            header("Connection", "keep-alive")
            header("Keep-Alive", "timeout=60")
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
            val maxEvents = 500 // Maximum event limit | 最大事件数限制
            
            while (shouldContinue && eventCount < maxEvents) {
                try {
                    incoming.collect { event ->
                        if (!isActive) {
                            shouldContinue = false
                            return@collect
                        }
                        
                        eventCount++
                        val eventType = event.event?.let { EventType.fromValue(it) }
                        // If event type is ERROR, end the connection | 如果事件类型是 ERROR，结束连接
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
                    shouldContinue = false
                    throw e
                }
            }
            
            if (eventCount >= maxEvents) {
                throw Exception("SSE reached maximum event limit")
            }
        }
    }

    /**
     * Create HTTP client with custom engine | 使用自定义引擎创建HTTP客户端
     * @param engine HTTP client engine | HTTP客户端引擎
     */
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
                val token = TokenManager.getToken(true)
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