package com.coze.api.chat

import com.coze.api.helper.APIBase
import com.coze.api.helper.RequestOptions
import com.coze.api.model.ApiResponse
import com.coze.api.model.ChatV3Message

/**
 * Message Service | 消息服务
 * Handles chat message operations | 处理聊天消息操作
 */
class MessageService : APIBase() {
    /**
     * List messages in a chat | 获取聊天消息列表
     * @param conversationId Conversation ID | 会话ID
     * @param chatId Chat ID | 聊天ID
     * @param options Request options | 请求选项
     * @return List<ChatV3Message> List of chat messages | 聊天消息列表
     */
    suspend fun list(
        conversationId: String,
        chatId: String,
        options: RequestOptions? = null
    ): ApiResponse<List<ChatV3Message>> {
        // Parameter validation | 参数验证
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