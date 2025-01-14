package com.coze.demo

import com.coze.api.conversation.ConversationService
import com.coze.api.model.ApiResponse
import com.coze.api.model.EnterMessage
import com.coze.api.model.conversation.*

/**
 * Conversation Demo | 对话演示
 * Demonstrates conversation management functionality | 演示对话管理功能
 */
class ConversationDemo {
    private val conversationService = ConversationService()
    private val botId = "7373880376026103809"

    /**
     * List all conversations with pagination | 分页列出所有对话
     * @param pageNum Page number | 页码
     * @param pageSize Page size | 每页大小
     * @return ApiResponse<ListConversationsData> List of conversations | 对话列表
     */
    suspend fun listConversations(pageNum: Int = 1, pageSize: Int = 50): ApiResponse<ListConversationsData> {
        val request = ListConversationReq(
            botId = botId,
            pageNum = pageNum,
            pageSize = pageSize
        )
        return conversationService.list(request)
    }

    /**
     * Create a new conversation | 创建新对话
     * @param messages Initial messages | 初始消息
     * @param metaData Additional metadata | 额外元数据
     * @return ApiResponse<Conversation> Created conversation | 创建的对话
     */
    suspend fun createConversation(
        messages: List<EnterMessage>? = null,
        metaData: Map<String, String>? = null
    ): ApiResponse<Conversation> {
        val request = CreateConversationReq(
            messages = messages,
            metaData = metaData,
            botId = botId
        )
        return conversationService.create(request)
    }

    /**
     * Get conversation details | 获取对话详情
     * @param conversationId Conversation ID | 对话ID
     * @return ApiResponse<Conversation> Conversation details | 对话详情
     */
    suspend fun getConversation(conversationId: String): ApiResponse<Conversation> {
        return conversationService.retrieve(conversationId)
    }

    /**
     * Clear a conversation | 清除对话
     * @param conversationId Conversation ID | 对话ID
     * @return ApiResponse<ConversationSession> Cleared conversation session | 清除的对话会话
     */
    suspend fun clearConversation(conversationId: String): ApiResponse<ConversationSession> {
        return conversationService.clear(conversationId)
    }
}
