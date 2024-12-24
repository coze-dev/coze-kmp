package com.coze.demo

import com.coze.api.conversation.MessageService
import com.coze.api.model.ChatV3Message
import com.coze.api.model.ContentType
import com.coze.api.model.RoleType
import com.coze.api.model.conversation.CreateMessageReq
import com.coze.api.model.conversation.ListMessageData
import com.coze.api.model.conversation.ListMessageReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext

class MessageDemo {
    private val messageService = MessageService()

    suspend fun listConversationMessages(
        conversationId: String,
        pageSize: Int = 20,
        pageToken: String? = null
    ): ListMessageData = withContext(Dispatchers.IO) {
        val request = ListMessageReq(
            limit = pageSize,
            beforeId = pageToken
        )
        messageService.list(conversationId, request)
    }

    suspend fun createConversationMessage(
        conversationId: String,
        content: String,
        role: String = "user",
        contentType: String = "text"
    ): ChatV3Message = withContext(Dispatchers.IO) {
        val request = CreateMessageReq(
            role = RoleType.valueOf(role.uppercase()),
            content = content,
            contentType = ContentType.valueOf(contentType.uppercase())
        )
        messageService.create(conversationId, request).data!!
    }

    suspend fun retrieveMessage(
        conversationId: String,
        messageId: String
    ): ChatV3Message = withContext(Dispatchers.IO) {
        println("Retrieving message $messageId from conversation $conversationId")
        val result = messageService.retrieve(conversationId, messageId).data!!
        println("Retrieved message: $result")
        result
    }

    suspend fun deleteConversationMessage(
        conversationId: String,
        messageId: String
    ): ChatV3Message = withContext(Dispatchers.IO) {
        println("Deleting message $messageId from conversation $conversationId")
        messageService.delete(conversationId, messageId).data!!
    }
} 