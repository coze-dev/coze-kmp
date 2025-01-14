package com.coze.api.conversation

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.conversation.*

/**
 * Conversation Service | 会话服务
 * Handles conversation operations including creation, retrieval, listing and clearing | 处理会话操作，包括创建、获取、列表和清除
 */
class ConversationService : APIBase() {
    /**
     * Create a conversation | 创建会话
     * A conversation is an interaction between an agent and a user | 会话是智能体和用户之间的交互
     * @param request Creation parameters | 创建参数
     * @param options Request options | 请求选项
     * @return Conversation Created conversation information | 创建的会话信息
     */
    suspend fun create(request: CreateConversationReq, options: RequestOptions? = null): ApiResponse<Conversation> {
        // Parameter validation | 参数验证
        require(!request.botId.isNullOrBlank()) { "botId cannot be empty" }
        return post("/v1/conversation/create", request, options)
    }

    /**
     * Retrieve conversation information | 获取会话信息
     * @param conversationId Conversation ID | 会话ID
     * @param options Request options | 请求选项
     * @return Conversation Conversation information | 会话信息
     */
    suspend fun retrieve(conversationId: String, options: RequestOptions? = null): ApiResponse<Conversation> {
        // Parameter validation | 参数验证
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        val params = mapOf(
            "conversation_id" to conversationId,
        )
        return get("/v1/conversation/retrieve", options?.copy(
            params = options.params + params
        ) ?: RequestOptions(params = params))
    }

    /**
     * List conversations | 获取会话列表
     * @param request Listing parameters | 列表参数
     * @param options Request options | 请求选项
     * @return ListConversationsData List of conversations | 会话列表数据
     */
    suspend fun list(request: ListConversationReq, options: RequestOptions? = null): ApiResponse<ListConversationsData> {
        // Parameter validation | 参数验证
        require(request.botId.isNotBlank()) { "botId cannot be empty" }
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
     * Clear conversation history | 清除会话历史
     * @param conversationId Conversation ID | 会话ID
     * @param options Request options | 请求选项
     * @return ConversationSession Cleared conversation session | 已清除的会话会话
     */
    suspend fun clear(conversationId: String, options: RequestOptions? = null): ApiResponse<ConversationSession> {
        // Parameter validation | 参数验证
        require(conversationId.isNotBlank()) { "conversationId cannot be empty" }
        return post("/v1/conversations/$conversationId/clear", null, options)
    }
} 