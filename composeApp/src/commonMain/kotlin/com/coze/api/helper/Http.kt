package com.coze.api.utils

import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

open class BaseHttpClient {
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
    }

    suspend fun request(url: String, method: HttpMethod, body: Any? = null): HttpResponse {
        return httpClient.request(url) {
            this.method = method
            headers {
                append(HttpHeaders.Authorization, "Bearer xxx")
                append(HttpHeaders.UserAgent, "ktor client")
            }
            contentType(ContentType.Application.Json)
            body?.let { setBody(it) }
        }
    }
}