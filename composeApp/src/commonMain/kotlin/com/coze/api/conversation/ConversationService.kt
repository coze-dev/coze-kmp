package com.coze.api.conversation

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.conversation.*

/**
 * Service class for managing conversations
 */
class ConversationService : APIBase() {
    /**
     * Create a conversation
     * Conversation is an interaction between an agent and a user, including one or more messages.
     *
     * @param request Parameters for creating a conversation
     * @param options Optional request options
     * @return Information about the created conversation
     */
    suspend fun create(request: CreateConversationReq, options: RequestOptions? = null): ApiResponse<Conversation> {
        return post("/v1/conversation/create", request, options)
    }

    /**
     * Get the information of specific conversation
     *
     * @param conversationId The ID of the conversation
     * @param botId The ID of the bot
     * @param options Optional request options
     * @return Information about the conversation
     */
    suspend fun retrieve(conversationId: String, options: RequestOptions? = null): ApiResponse<Conversation> {
        val params = mapOf(
            "conversation_id" to conversationId,
        )
        return get("/v1/conversation/retrieve", options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * List all conversations
     *
     * @param request Parameters for listing conversations
     * @param options Optional request options
     * @return Information about the conversations
     */
    suspend fun list(request: ListConversationReq, options: RequestOptions? = null): ApiResponse<ListConversationsData> {
        val params = mapOf(
            "bot_id" to request.botId,
            "page_num" to (request.pageNum ?: 1).toString(),
            "page_size" to (request.pageSize ?: 50).toString()
        )
        return get("/v1/conversations", options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * Clear a conversation
     *
     * @param conversationId The ID of the conversation
     * @param options Optional request options
     * @return Information about the conversation session
     */
    suspend fun clear(conversationId: String, options: RequestOptions? = null): ApiResponse<ConversationSession> {
        return post("/v1/conversations/$conversationId/clear", null, options)
    }
} 