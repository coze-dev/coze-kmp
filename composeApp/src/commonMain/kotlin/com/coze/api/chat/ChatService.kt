package com.coze.api.chat

import com.coze.api.utils.BaseHttpClient
import com.coze.api.model.chat.*
import com.coze.api.model.ApiResponse
import io.ktor.client.call.*
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod

class ChatService : BaseHttpClient() {

    suspend fun getChatData(): String {
        val url = "https://api.coze.com/v3/chat"
        val body = ChatRequest(
            botId = "7373880376026103809",
            userId = "007",
            additionalMessages = listOf(
                Message(role = "user", content = "hi there")
            )
        )

        val response: HttpResponse = this.request(url, HttpMethod.Post, body)
        val rsp: ApiResponse<ChatResponse> = response.body()
        val chatId = rsp.data.id
        val conversationId = rsp.data.conversationId
        return "(Msg Sent. chat_id=$chatId, conversation_id=$conversationId)"
    }
}
