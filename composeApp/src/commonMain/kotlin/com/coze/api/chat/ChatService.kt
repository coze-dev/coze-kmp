package com.coze.api.chat

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.chat.*
import com.coze.api.model.ApiResponse
import com.coze.api.model.EventType
import com.coze.api.model.ChatStatus
import com.coze.api.model.ChatV3Message
import com.coze.api.model.EnterMessage
import com.coze.api.model.StreamChatData
import com.coze.api.model.sseEvent2ChatData
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlin.random.Random
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

const val POLL_INTERVAL: Long = 1000
const val POLL_TIMEOUT: Long = 60000

fun generateUUID(): String = 
    (Random.nextDouble() * Clock.System.now().toEpochMilliseconds()).toString()

class ChatService : APIBase() {

    suspend fun createChat(
        params: CreateChatReq, 
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(!params.additionalMessages.isNullOrEmpty()) { "additionalMessages cannot be empty" }
        
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = false
        )
        return post(apiUrl, payload, options)
    }

    suspend fun createAndPollChat(
        params: CreateChatReq, 
        options: RequestOptions? = null
    ): CreateChatPollData {
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(!params.additionalMessages.isNullOrEmpty()) { "additionalMessages cannot be empty" }
        
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = false
        )
        val response = post<CreateChatData>(
            path = apiUrl,
            payload = payload,
            options = options
        )
        val chatId = response.data?.id ?: throw Exception("创建聊天失败：返回数据为空")
        val conversationId = response.data.conversationId
        var chat: CreateChatData?

        val startTime = Clock.System.now()
        while (true) {
            delay(POLL_INTERVAL)
            chat = retrieveChat(conversationId, chatId).data
            if (chat != null) {
                if (chat.status in listOf(ChatStatus.COMPLETED, ChatStatus.FAILED, ChatStatus.REQUIRES_ACTION)) {
                    break
                }
            }
            if (Clock.System.now().toEpochMilliseconds() - startTime.toEpochMilliseconds() > POLL_TIMEOUT) {
                throw Exception("轮询超时")
            }
        }

        val nonNullChat = requireNotNull(chat) { "The Response Chat Message data should not be null" }
        val messageList = listMessages(conversationId, chatId).data
        return CreateChatPollData(nonNullChat, messageList)
    }

    private suspend fun retrieveChat(
        conversationId: String, 
        chatId: String, 
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(chatId.isNotBlank()) { "chatId cannot be empty" }
        
        println("retrieving...")
        val apiUrl = "/v3/chat/retrieve?conversation_id=$conversationId&chat_id=$chatId"
        return post<CreateChatData>(
            path = apiUrl,
            payload = null,
            options = options
        )
    }

    suspend fun cancelChat(
        conversationId: String, 
        chatId: String, 
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(chatId.isNotBlank()) { "chatId cannot be empty" }
        
        return post<CreateChatData>("/v3/chat/cancel", mapOf(
            "conversation_id" to conversationId,
            "chat_id" to chatId
        ), options)
    }

    private suspend fun listMessages(
        conversationId: String, 
        chatId: String,
    ): ApiResponse<List<ChatV3Message>> {
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(chatId.isNotBlank()) { "chatId cannot be empty" }
        
        return get<List<ChatV3Message>>("/v3/chat/message/list", 
        RequestOptions(
            params = mapOf(
                "conversation_id" to conversationId,
                "chat_id" to chatId
            )
        ))
    }

    private fun handleAdditionalMessages(additionalMessages: List<EnterMessage>?): List<EnterMessage>? {
        return additionalMessages?.map { it.copy(content = it.content ?: "") }
    }

    suspend fun submitToolOutputs(
        params: SubmitToolOutputsReq,
        options: RequestOptions? = null
    ): ApiResponse<CreateChatData> {
        require(params.conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(params.chatId.isNotBlank()) { "chatId cannot be empty" }
        require(params.toolOutputs.isNotEmpty()) { "toolOutputs cannot be empty" }
        
        val apiUrl = "/v3/chat/submit_tool_outputs?conversation_id=${params.conversationId}&chat_id=${params.chatId}"
        return post(apiUrl, params, options)
    }

    suspend fun submitToolOutputsStream(
        params: SubmitToolOutputsReq,
        options: RequestOptions? = null
    ): Flow<StreamChatData> = flow {
        require(params.conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(params.chatId.isNotBlank()) { "chatId cannot be empty" }
        require(params.toolOutputs.isNotEmpty()) { "toolOutputs cannot be empty" }
        
        val apiUrl = "/v3/chat/submit_tool_outputs?conversation_id=${params.conversationId}&chat_id=${params.chatId}"
        val eventFlow = sse(apiUrl, params, options ?: RequestOptions())
        eventFlow.collect { event ->
            val chatData = sseEvent2ChatData(event)
            emit(chatData)
            // If event is "[DONE]", end
            if (chatData.event == EventType.DONE) {
                println("SSE DONE.")
                return@collect
            }
        }
    }

    fun stream(
        params: StreamChatReq, 
        options: RequestOptions? = null
    ): Flow<StreamChatData> = flow {
        require(params.botId.isNotBlank()) { "botId cannot be empty" }
        require(!params.additionalMessages.isNullOrEmpty()) { "additionalMessages cannot be empty" }
        
        params.userId = params.userId.takeUnless { it.isNullOrEmpty() } ?: generateUUID()
        val apiUrl = "/v3/chat${params.conversationId?.let { "?conversation_id=$it" } ?: ""}"
        val payload = params.copy(
            additionalMessages = handleAdditionalMessages(params.additionalMessages),
            stream = true
        )

        val eventFlow = sse(apiUrl, payload, options ?: RequestOptions())
        eventFlow.collect { event ->
            val chatData = sseEvent2ChatData(event)
            emit(chatData)
            // If event is "[DONE]", end
            if (chatData.event == EventType.DONE) {
                println("SSE DONE.")
                return@collect
            }
        }
    }
}
