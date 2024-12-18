package com.coze.demo

import com.coze.api.chat.ChatService
import com.coze.api.model.ChatEventType
import com.coze.api.model.chat.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class ChatDemo {
    companion object {
        private var token: String? = null
            get() {
                if (field == null) {
                    field = AuthDemo.getJWTAuth()
                }
                return field
            }
    }

    // 使用伴生对象中的 token 初始化 chatService
    private val chatService = ChatService(token!!)
    

    private val defaultBotId = "7373880376026103809"
    private val defaultUserId = "007"

    fun noneStreamCreate(): Flow<String> = flow {
        val greeting = "hi there"
        emit(greeting)
        emit("(Visiting the chat v3 API!)...")

        // Send request to get data
        val data = chatService.createChat(
            ChatRequest(
                botId = defaultBotId,
                userId = defaultUserId,
                additionalMessages = listOf(Message(role = "user", content = greeting))
            )
        )
        emit("(Response arrived. chat_id=${data.id}, conversation_id=${data.conversationId})")
    }

    fun streamTest(msg: String=""): Flow<String> = flow {
        val messageContent = msg.ifBlank { "Tell me 3 famous jokes" }
        val streamFlow = chatService.stream(
            ChatRequest(
                botId = defaultBotId,
                userId = defaultUserId,
                additionalMessages = listOf(
                    Message(role = "user", content = messageContent)
                )
            )
        )
        
        // Collect events from the stream and emit them
        streamFlow.collect { event ->
            if (event is ChatMessageEvent && event.event == ChatEventType.CONVERSATION_MESSAGE_DELTA) {
                emit(event.data.content ?: " ")
            }
        }
    }

    fun noneStreamCreateAndPoll(msg: String=""): Flow<String> = flow {
        val messageContent = msg.ifBlank { "Tell me 3 famous jokes" }
        emit("(Visiting the chat v3 API!)...")

        // Send request to get data
        val data = chatService.createAndPollChat(
            ChatRequest(
                botId = defaultBotId,
                userId = defaultUserId,
                additionalMessages = listOf(Message(role = "user", content = messageContent))
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
}
