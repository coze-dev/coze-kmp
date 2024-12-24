package com.coze.api.model.chat

import com.coze.api.model.*
import io.ktor.sse.ServerSentEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class ChatResponse(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String
)

@Serializable
data class CreateChatData(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("bot_id")
    val botId: String,
    val status: ChatStatus? = null,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("completed_at")
    val completedAt: Long? = null,
    @SerialName("failed_at")
    val failedAt: Long? = null,
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null,
    @SerialName("last_error")
    val lastError: ErrorData? = null,
    @SerialName("required_action")
    val requiredAction: RequiredActionType? = null,
    val usage: Usage? = null
)

@Serializable
data class RequiredActionType(
    @SerialName("type")
    val type: String,
    @SerialName("submit_tool_outputs")
    val submitToolOutputs: SubmitToolOutputs
)

@Serializable
data class SubmitToolOutputs(
    @SerialName("tool_calls")
    val toolCalls: List<ToolCallType>
)

@Serializable
data class ToolCallType(
    @SerialName("id")
    val id: String,
    @SerialName("type")
    val type: String,
    @SerialName("function")
    val function: FunctionCall
)

@Serializable
data class FunctionCall(
    @SerialName("name")
    val name: String,
    @SerialName("argument")
    val argument: String
)

@Serializable
data class CreateChatPollData(
    val chat: CreateChatData,
    val messages: List<ChatV3Message>? = null,
    val usage: Usage? = null
)

// Define the return data type for stream
@Serializable
sealed class StreamChatData {
    abstract val event: ChatEventType
}

@Serializable
data class CreateChatEvent(
    // event:
    // | ChatEventType.CONVERSATION_CHAT_CREATED
    // | ChatEventType.CONVERSATION_CHAT_IN_PROGRESS
    // | ChatEventType.CONVERSATION_CHAT_COMPLETED
    // | ChatEventType.CONVERSATION_CHAT_FAILED
    // | ChatEventType.CONVERSATION_CHAT_REQUIRES_ACTION;
    override val event: ChatEventType,
    val data: CreateChatData
) : StreamChatData()

@Serializable
data class ChatMessageEvent(
    // event:
    //        | ChatEventType.CONVERSATION_MESSAGE_DELTA
    //        | ChatEventType.CONVERSATION_MESSAGE_COMPLETED
    //        | ChatEventType.CONVERSATION_AUDIO_DELTA;
    override val event: ChatEventType,
    val data: ChatV3Message
) : StreamChatData()

@Serializable
data class DoneEvent(
    override val event: ChatEventType = ChatEventType.DONE,
    val data: String = "[DONE]"
) : StreamChatData()

@Serializable
data class ErrorEvent(
    override val event: ChatEventType = ChatEventType.ERROR,
    val data: ErrorData
) : StreamChatData()

fun sseEvent2ChatData(event: ServerSentEvent): StreamChatData {
    val eventType = try {
        event.event?.let { ChatEventType.fromValue(it) }
    } catch (e: IllegalArgumentException) {
        ChatEventType.ERROR // If conversion throws an exception, directly convert to Error
    }

    val jsonFormat = Json { ignoreUnknownKeys = true }

    return when (eventType) {
        ChatEventType.ERROR -> {
            val errorData = jsonFormat.decodeFromString<ErrorData>(event.data ?: "")
            ErrorEvent(data = errorData)
        }
        ChatEventType.CONVERSATION_MESSAGE_DELTA,
        ChatEventType.CONVERSATION_MESSAGE_COMPLETED,
        ChatEventType.CONVERSATION_AUDIO_DELTA -> {
            val message = jsonFormat.decodeFromString<ChatV3Message>(event.data ?: "")
            ChatMessageEvent(eventType, message)
        }
        ChatEventType.CONVERSATION_CHAT_CREATED,
        ChatEventType.CONVERSATION_CHAT_IN_PROGRESS,
        ChatEventType.CONVERSATION_CHAT_COMPLETED,
        ChatEventType.CONVERSATION_CHAT_FAILED,
        ChatEventType.CONVERSATION_CHAT_REQUIRES_ACTION -> {
            val chatData = jsonFormat.decodeFromString<CreateChatData>(event.data ?: "")
            CreateChatEvent(eventType, chatData)
        }
        ChatEventType.DONE -> DoneEvent()
        else -> throw IllegalArgumentException("Unsupported event type: $eventType")
    }
}
