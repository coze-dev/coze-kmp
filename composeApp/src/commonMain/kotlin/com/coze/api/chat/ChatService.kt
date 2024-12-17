package com.coze.api.chat

import com.coze.api.helper.APIClient
import com.coze.api.helper.RequestOptions
import com.coze.api.model.chat.*
import com.coze.api.model.ApiResponse
import com.coze.api.model.ChatEventType
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpMethod
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.serializer

fun generateUUID(): String = 
    (Random.nextDouble() * Clock.System.now().toEpochMilliseconds()).toString()

class ChatService(token: String) : APIClient(token) {

    suspend fun createChat(
        params: CreateChatReq, 
        options: RequestOptions? = null
    ): CreateChatData {
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = false
        )
        val response = post(
            path = apiUrl,
            payload = payload,
            serializer = serializer<ApiResponse<CreateChatData>>(),
            options = options
        )
        return response.data
    }

    suspend fun createAndPollChat(
        params: CreateChatReq, 
        options: RequestOptions? = null
    ): CreateChatPollData {
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = false
        )
        val response = post(
            path = apiUrl,
            payload = payload,
            serializer = serializer<ApiResponse<CreateChatData>>(),
            options = options
        )

        val chatId = response.data.id
        val conversationId = response.data.conversationId
        var chat: CreateChatData?

        val startTime = Clock.System.now()
        val timeout = 60000 
        while (true) {
            delay(500)
            chat = retrieveChat(conversationId, chatId)
            if (chat.status in listOf(ChatStatus.COMPLETED, ChatStatus.FAILED, ChatStatus.REQUIRES_ACTION)) {
                break
            }
            if (Clock.System.now().toEpochMilliseconds() - startTime.toEpochMilliseconds() > timeout) {
                throw Exception("轮询超时")
            }
        }

        val nonNullChat = requireNotNull(chat) { "The Response Chat Message data should not be null" }
        val messageList = listMessages(conversationId, chatId)
        return CreateChatPollData(nonNullChat, messageList)
    }

    suspend fun retrieveChat(
        conversationId: String, 
        chatId: String, 
        options: RequestOptions? = null
    ): CreateChatData {
        println("retrieving...")
        val apiUrl = "/v3/chat/retrieve?conversation_id=$conversationId&chat_id=$chatId"
        val response = post(
            path = apiUrl,
            payload = null,
            serializer = serializer<ApiResponse<CreateChatData>>(),
            options = options
        )
        return response.data
    }

    suspend fun cancelChat(
        conversationId: String, 
        chatId: String, 
        options: RequestOptions? = null
    ): CreateChatData {
        val apiUrl = "/v3/chat/cancel"
        val payload = mapOf("conversation_id" to conversationId, "chat_id" to chatId)
        val response = post(
            path = apiUrl,
            payload = payload,
            serializer = serializer<ApiResponse<CreateChatData>>(),
            options = options
        )
        return response.data
    }

    private suspend fun listMessages(
        conversationId: String, 
        chatId: String
    ): List<ChatV3Message> {
        val apiUrl = "/v3/chat/message/list?conversation_id=$conversationId&chat_id=$chatId"
        val response = get(
            path = apiUrl,
            serializer = serializer<ApiResponse<List<ChatV3Message>>>()
        )
        return response.data
    }

    private fun handleAdditionalMessages(additionalMessages: List<Message>?): List<Message>? {
        return additionalMessages?.map { it.copy(content = it.content ?: "") }
    }

    fun stream(
        params: StreamChatReq, 
        options: RequestOptions? = null
    ): Flow<StreamChatData> = flow {
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = true
        )

        val eventFlow = sse(apiUrl, payload, true, options ?: RequestOptions())
        eventFlow.collect { event ->
            val chatData = sseEvent2ChatData(event)
            emit(chatData)
            // If event is "[DONE]", end
            if (chatData.event == ChatEventType.DONE) {
                println("SSE DONE.")
                return@collect
            }
        }
    }
}
