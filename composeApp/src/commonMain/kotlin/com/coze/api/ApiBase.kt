package com.coze.api

import RocketLaunch
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import io.ktor.client.request.*
import io.ktor.client.call.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.*

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import io.ktor.client.plugins.logging.*

// Define EnterMessage as a simple message structure
@Serializable
data class EnterMessage(
    val role: String,
    val content: String,
    var content_type: String = "text"
)

// Define ChatRequest as the body of the chat request
@Serializable
data class ChatRequest(
    val bot_id: String,
    val user_id: String,
    val additional_messages: List<EnterMessage> = emptyList()
)

// Define request return type
@Serializable
data class ApiResponse<T>(
    val code: Int,
    val msg: String = "",
    val data: T
)

@Serializable
data class ChatMessage(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
)

class ApiBase {
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
            // Do not log token
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }
    private suspend fun chat(): String {
        val raw: HttpResponse = httpClient.request("https://api.coze.com/v3/chat"){
            headers {
                append(HttpHeaders.Authorization, "Bearer xxx")
                append(HttpHeaders.UserAgent, "ktor client")
            }
            contentType(ContentType.Application.Json)
            method = HttpMethod.Post

            val body = ChatRequest(
                bot_id = "7373880376026103809",
                user_id = "007",
                additional_messages = listOf(
                    EnterMessage(role = "user", content = "hi there")
                )
            )
            setBody(body) // This will be automatically serialized to JSON
        }

        val rsp: ApiResponse<ChatMessage> = raw.body()
        val chatId = rsp.data.id
        val conversationId = rsp.data.id
        return "(Msg Sent. chat_id=$chatId, conversation_id=$conversationId)"
    }

    suspend fun launchPhrase(): String =
        try {
            "User: hi there~ \nResponse: ${chat()} 🚀"
        } catch (e: Exception) {
            println("Exception: $e")
            "Error occurred"
        }
}