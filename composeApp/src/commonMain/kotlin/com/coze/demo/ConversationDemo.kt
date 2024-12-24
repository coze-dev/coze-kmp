package com.coze.demo

import com.coze.api.conversation.ConversationService
import com.coze.api.model.ApiResponse
import com.coze.api.model.EnterMessage
import com.coze.api.model.conversation.*

/**
 * Demo class for conversation management functionality
 */
class ConversationDemo {
    private val conversationService = ConversationService()
    private val botId = "7373880376026103809"

    /**
     * List all conversations with pagination support
     */
    suspend fun listConversations(pageNum: Int = 1, pageSize: Int = 50): ApiResponse<ListConversationsData> {
        return try {
            val request = ListConversationReq(
                botId = botId,
                pageNum = pageNum,
                pageSize = pageSize
            )
            conversationService.list(request)
        } catch (e: Exception) {
            println("[ConversationDemo] List conversations failed: ${e.message}")
            throw e
        }
    }

    /**
     * Create a new conversation with optional messages and metadata
     */
    suspend fun createConversation(
        messages: List<EnterMessage>? = null,
        metaData: Map<String, String>? = null
    ): ApiResponse<Conversation> {
        return try {
            val request = CreateConversationReq(
                messages = messages,
                metaData = metaData,
                botId = botId
            )
            conversationService.create(request)
        } catch (e: Exception) {
            println("[ConversationDemo] Create conversation failed: ${e.message}")
            throw e
        }
    }

    /**
     * Get conversation details by ID
     */
    suspend fun getConversation(conversationId: String): ApiResponse<Conversation> {
        return try {
            conversationService.retrieve(conversationId)
        } catch (e: Exception) {
            println("[ConversationDemo] Get conversation failed: ${e.message}")
            throw e
        }
    }

    /**
     * Clear a conversation by ID
     */
    suspend fun clearConversation(conversationId: String): ApiResponse<ConversationSession> {
        return try {
            conversationService.clear(conversationId)
        } catch (e: Exception) {
            println("[ConversationDemo] Clear conversation failed: ${e.message}")
            throw e
        }
    }
}
