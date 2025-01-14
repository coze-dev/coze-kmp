package com.coze.api.chat

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.ChatV3Message

/**
 * Service class for managing chat messages
 */
class MessageService : APIBase() {
    /**
     * Get the list of messages in a chat
     *
     * @param conversationId The ID of the conversation
     * @param chatId The ID of the chat
     * @param options Optional request options
     * @return An array of chat messages
     */
    suspend fun list(
        conversationId: String,
        chatId: String,
        options: RequestOptions? = null
    ): ApiResponse<List<ChatV3Message>> {
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(chatId.isNotBlank()) { "chatId cannot be empty" }
        
        val params = mapOf(
            "conversation_id" to conversationId,
            "chat_id" to chatId
        )
        return get("/v3/chat/message/list", options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }
} 