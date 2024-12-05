package com.coze.api.chat

import com.coze.api.model.*
import com.coze.api.model.chat.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import io.ktor.client.plugins.sse.*
import io.ktor.client.engine.cio.*
import io.ktor.http.ContentType
import io.ktor.sse.ServerSentEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

// Define RequestOptions type
data class RequestOptions(
    val headers: Map<String, String> = emptyMap()
)

open class BaseHttpClient {
    private val apiUrlPrefix = "https://api.coze.com/"
    private val ua = "ktor client"
    private val token = "pat_EuEHpAsdJD5tbf34HI1cfj7qGltE0ySwAqLpMb3jBlZwpbdxEovovVb9T3lR4i90"

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

    suspend fun makeRequest(
        urlPath: String,
        method: HttpMethod,
        body: Any? = null,
        options: RequestOptions? = RequestOptions()
    ): HttpResponse {
        return httpClient.request(apiUrlPrefix + urlPath) {
            this.method = method
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
                append(HttpHeaders.UserAgent, ua)
                options?.headers?.forEach { (key, value) -> append(key, value) }
            }
            if (method != HttpMethod.Get) {
                contentType(ContentType.Application.Json)
                body?.let { setBody(it) }
            }
        }
    }

    suspend fun get(urlPath: String, options: RequestOptions? = RequestOptions()): HttpResponse {
        return makeRequest(urlPath, HttpMethod.Get, options = options)
    }

    suspend fun post(
        urlPath: String,
        body: Any? = null,
        options: RequestOptions? = RequestOptions()
    ): HttpResponse {
        return makeRequest(urlPath, HttpMethod.Post, body, options)
    }

    suspend fun sse(
        urlPath: String,
        body: Any? = null,
        options: RequestOptions? = RequestOptions()
    ): Flow<ServerSentEvent> {
        return flow {
            httpClient.sse(apiUrlPrefix + urlPath, {
                method = HttpMethod.Post
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.UserAgent, ua)
                    options?.headers?.forEach { (key, value) -> append(key, value) }
                }
                contentType(ContentType.Application.Json)
                body?.let { setBody(it) }
            }) {
                var shouldContinue = true
                while (shouldContinue) {
                    incoming.collect { event ->
                        // println("event type=${event.event}")
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
}