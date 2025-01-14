package com.coze.demo

import com.coze.api.chat.ChatService
import com.coze.api.chat.MessageService
import com.coze.api.helper.RequestOptions
import com.coze.api.model.EventType
import com.coze.api.model.ChatStatus
import com.coze.api.model.ChatV3Message
import com.coze.api.model.EnterMessage
import com.coze.api.model.MessageType
import com.coze.api.model.RoleType
import com.coze.api.model.StreamChatData
import com.coze.api.model.chat.*
import com.coze.api.model.conversation.ListMessageReq
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

/**
 * Chat Demo | 聊天演示
 * Demonstrates chat functionality | 演示聊天功能
 */
class ChatDemo {
    private val chatService = ChatService()
    private val messageService = MessageService()
    
    private val defaultBotId = "7373880376026103809"
    private val defaultUserId = "007"

    /**
     * Create chat without streaming | 不使用流式创建聊天
     * @return Flow<String> Chat events | 聊天事件流
     */
    fun noneStreamCreate(): Flow<String> = flow {
        val greeting = "hi there"
        emit(greeting)
        emit("(Visiting the chat v3 API!)...")

        // Send request to get data | 发送请求获取数据
        val data = chatService.createChat(
            ChatRequest(
                botId = defaultBotId,
                userId = defaultUserId,
                additionalMessages = listOf(EnterMessage(role = RoleType.USER, content = greeting))
            )
        ).data
        if (data != null) {
            emit("(Response arrived. chat_id=${data.id}, conversation_id=${data.conversationId})")
        }
    }

    /**
     * Test chat streaming | 测试聊天流式处理
     * @param msg Input message | 输入消息
     * @return Flow<String> Chat events | 聊天事件流
     */
    fun streamTest(msg: String=""): Flow<String> = flow {
        val messageContent = msg.ifBlank { "Tell me 3 famous jokes" }
        val streamFlow = chatService.stream(
            ChatRequest(
                botId = defaultBotId,
                userId = defaultUserId,
                additionalMessages = listOf(
                    EnterMessage(role = RoleType.USER, content = messageContent)
                )
            )
        )
        
        // Collect events from the stream and emit them | 从流中收集事件并发送
        streamFlow.collect { event ->
            if (event is StreamChatData.ChatMessageEvent && event.event == EventType.CONVERSATION_MESSAGE_DELTA) {
                emit(event.data.content ?: " ")
            }
        }
    }

    /**
     * Create chat and poll without streaming | 不使用流式创建聊天并轮询
     * @param msg Input message | 输入消息
     * @return Flow<String> Chat events | 聊天事件流
     */
    fun noneStreamCreateAndPoll(msg: String=""): Flow<String> = flow {
        val messageContent = msg.ifBlank { "Tell me 3 famous jokes" }
        emit("(Visiting the chat v3 API!)...")

        // Send request to get data | 发送请求获取数据
        val data = chatService.createAndPollChat(
            ChatRequest(
                botId = defaultBotId,
                userId = defaultUserId,
                additionalMessages = listOf(EnterMessage(role = RoleType.USER, content = messageContent))
            )
        )
        emit("(Response arrived.)")

        if (data.chat.status == ChatStatus.COMPLETED) {
            data.messages?.forEach { item ->
                val prefix = if (item.type != MessageType.ANSWER) "> " else ""
                emit("$prefix[${item.role}]:[${item.type}]:${item.content}")
            }
            emit("usage: ${data.chat.usage}")
        } else {
            emit("No messages")
        }
    }

    /**
     * List chat messages | 列出聊天消息
     * @param conversationId Conversation ID | 对话ID
     * @param chatId Chat ID | 聊天ID
     * @return List<ChatV3Message> List of messages | 消息列表
     */
    suspend fun listChatMessages(
        conversationId: String,
        chatId: String,
    ): List<ChatV3Message> = withContext(Dispatchers.IO) {
        messageService.list(conversationId, chatId).data?: emptyList()
    }
}
