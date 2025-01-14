package com.coze.api.conversation

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.ChatV3Message
import com.coze.api.model.conversation.*

/**
 * Conversation Message Service | 会话消息服务
 * Handles conversation message operations | 处理会话消息操作
 */
class MessageService : APIBase() {
    /**
     * Create a message in conversation | 在会话中创建消息
     * @param conversationId Conversation ID | 会话ID
     * @param request Message creation parameters | 消息创建参数
     * @param options Request options | 请求选项
     * @return ChatV3Message Created message information | 创建的消息信息
     */
    suspend fun create(
        conversationId: String,
        request: CreateMessageReq,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        // Parameter validation | 参数验证
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        val params = mapOf("conversation_id" to conversationId)
        return post("/v1/conversation/message/create", request, options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * Update message content | 修改消息内容
     * @param conversationId Conversation ID | 会话ID
     * @param messageId Message ID | 消息ID
     * @param request Message update parameters | 消息更新参数
     * @param options Request options | 请求选项
     * @return ChatV3Message Updated message information | 更新后的消息信息
     */
    suspend fun update(
        conversationId: String,
        messageId: String,
        request: UpdateMessageReq,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        // Parameter validation | 参数验证
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(messageId.isNotBlank()) { "messageId cannot be empty" }
        val params = mapOf(
            "conversation_id" to conversationId,
            "message_id" to messageId
        )
        return post("/v1/conversation/message/modify", request, options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * Retrieve message information | 获取消息信息
     * @param conversationId Conversation ID | 会话ID
     * @param messageId Message ID | 消息ID
     * @param options Request options | 请求选项
     * @return ChatV3Message Message information | 消息信息
     */
    suspend fun retrieve(
        conversationId: String,
        messageId: String,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        // Parameter validation | 参数验证
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(messageId.isNotBlank()) { "messageId cannot be empty" }

        val params = mapOf(
            "conversation_id" to conversationId,
            "message_id" to messageId
        )
        return get("/v1/conversation/message/retrieve", options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * List messages in conversation | 获取会话消息列表
     * @param conversationId Conversation ID | 会话ID
     * @param request Listing parameters | 列表参数
     * @param options Request options | 请求选项
     * @return ListMessageData List of messages | 消息列表数据
     */
    suspend fun list(
        conversationId: String,
        request: ListMessageReq? = null,
        options: RequestOptions? = null
    ): ListMessageData {
        // Parameter validation | 参数验证
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }

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
     * Delete message from conversation | 从会话中删除消息
     * @param conversationId Conversation ID | 会话ID
     * @param messageId Message ID | 消息ID
     * @param options Request options | 请求选项
     * @return ChatV3Message Deleted message information | 已删除的消息信息
     */
    suspend fun delete(
        conversationId: String,
        messageId: String,
        options: RequestOptions? = null
    ): ApiResponse<ChatV3Message> {
        // Parameter validation | 参数验证
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        require(messageId.isNotBlank()) { "messageId cannot be empty" }

        val params = mapOf(
            "conversation_id" to conversationId,
            "message_id" to messageId
        )
        return post("/v1/conversation/message/delete", null, options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }
} 