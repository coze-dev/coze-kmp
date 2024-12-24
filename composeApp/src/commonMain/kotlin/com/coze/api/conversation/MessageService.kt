package com.coze.api.conversation

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.ChatV3Message
import com.coze.api.model.conversation.*

/**
 * Service class for managing conversation messages
 */
class MessageService : APIBase() {
    /**
     * Create a message and add it to the specified conversation
     *
     * @param conversationId The ID of the conversation
     * @param request Parameters for creating a message
     * @param options Optional request options
     * @return Information about the new message
     */
    suspend fun create(
        conversationId: String,
        request: CreateMessageReq,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        val params = mapOf("conversation_id" to conversationId)
        return post("/v1/conversation/message/create", request, options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * Modify a message
     *
     * @param conversationId The ID of the conversation
     * @param messageId The ID of the message
     * @param request Parameters for modifying a message
     * @param options Optional request options
     * @return Information about the modified message
     */
    suspend fun update(
        conversationId: String,
        messageId: String,
        request: UpdateMessageReq,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        val params = mapOf(
            "conversation_id" to conversationId,
            "message_id" to messageId
        )
        return post("/v1/conversation/message/modify", request, options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * Get the detailed information of specified message
     *
     * @param conversationId The ID of the conversation
     * @param messageId The ID of the message
     * @param options Optional request options
     * @return Information about the message
     */
    suspend fun retrieve(
        conversationId: String,
        messageId: String,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        val params = mapOf(
            "conversation_id" to conversationId,
            "message_id" to messageId
        )
        return get("/v1/conversation/message/retrieve", options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * List messages in a conversation
     *
     * @param conversationId The ID of the conversation
     * @param request Parameters for listing messages
     * @param options Optional request options
     * @return A list of messages
     */
    suspend fun list(
        conversationId: String,
        request: ListMessageReq? = null,
        options: RequestOptions? = null
    ): ListMessageData {
        val params = buildMap {
            put("conversation_id", conversationId)
            request?.order?.let { put("order", it) }
            request?.chatId?.let { put("chat_id", it) }
            request?.beforeId?.let { put("before_id", it) }
            request?.afterId?.let { put("after_id", it) }
            request?.limit?.let { put("limit", it.toString()) }
        }
        return getClient().get<ListMessageData>("/v1/conversation/message/list", options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * Delete a message within a specified conversation
     *
     * @param conversationId The ID of the conversation
     * @param messageId The ID of the message
     * @param options Optional request options
     * @return Details of the deleted message
     */
    suspend fun delete(
        conversationId: String,
        messageId: String,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        val params = mapOf(
            "conversation_id" to conversationId,
            "message_id" to messageId
        )
        return post("/v1/conversation/message/delete", null, options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }
} 