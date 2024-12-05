package com.coze.api.model.chat

import com.coze.api.model.*
import com.coze.api.model.chat.CreateChatPollData.Usage
import io.ktor.sse.ServerSentEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class Message(
    val role: String,
    val content: String? = null,
    @SerialName("content_type")
    var contentType: String? = "text",
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null,
    @SerialName("type")
    val type: String? = null
)

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
    val usage: UsageData? = null
)

@Serializable
data class UsageData(
    @SerialName("token_count")
    val tokenCount: Int,
    @SerialName("output_count")
    val outputCount: Int,
    @SerialName("input_count")
    val inputCount: Int
)

@Serializable
data class ChatV3Message(
    val id: String, // Unique identifier for the message
    @SerialName("conversation_id")
    val conversationId: String, // ID of the conversation this message belongs to
    @SerialName("bot_id")
    val botId: String, // Bot ID that created this message
    @SerialName("chat_id")
    val chatId: String? = null, // Chat ID
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null, // Additional data when creating the message
    @SerialName("role")
    val role: RoleType? = null, // Entity sending this message, e.g., user or assistant
    val content: String? = null, // Content of the message
    @SerialName("content_type")
    val contentType: ContentType? = null, // Type of the message content
    val status: String? = null,
    val usage: Usage? = null,
    @SerialName("created_at")
    val createdAt: Long? = null, // Creation time of the message
    @SerialName("updated_at")
    val updatedAt: Long? = null, // Update time of the message
    @SerialName("type")
    val type: MessageType? = null, // Type of the message
    @SerialName("last_error")
    val lastError: ErrorData = ErrorData(0, "")
)

@Serializable
enum class RoleType {
    @SerialName("user")
    USER,
    @SerialName("assistant")
    ASSISTANT
}

@Serializable
enum class ContentType {
    @SerialName("text")
    TEXT,
    @SerialName("object_string")
    OBJECT_STRING,
    @SerialName("card")
    CARD
}

@Serializable
enum class MessageType {
    @SerialName("question")
    QUESTION,
    @SerialName("answer")
    ANSWER,
    @SerialName("function_call")
    FUNCTION_CALL,
    @SerialName("tool_output")
    TOOL_OUTPUT,
    @SerialName("tool_response")
    TOOL_RESPONSE,
    @SerialName("follow_up")
    FOLLOW_UP,
    @SerialName("verbose")
    VERBOSE
}

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
enum class ChatStatus {
    @SerialName("created")
    CREATED,
    @SerialName("in_progress")
    IN_PROGRESS,
    @SerialName("completed")
    COMPLETED,
    @SerialName("failed")
    FAILED,
    @SerialName("requires_action")
    REQUIRES_ACTION,
    @SerialName("canceled")
    CANCELED
}

@Serializable
data class CreateChatPollData(
    val chat: CreateChatData,
    val messages: List<ChatV3Message>? = null,
    val usage: Usage? = null
) {
    @Serializable
    data class Usage(
        val tokenCount: Int, // Total tokens consumed in this conversation, including input and output
        val outputCount: Int, // Total tokens consumed in the output part
        val inputCount: Int // Total tokens consumed in the input part
    )
}

// Define the return data type for stream
@Serializable
sealed class StreamChatData {
    abstract val event: ChatEventType
}

@Serializable
data class CreateChatEvent(
    // event:
    //        | ChatEventType.CONVERSATION_CHAT_CREATED
    //        | ChatEventType.CONVERSATION_CHAT_IN_PROGRESS
    //        | ChatEventType.CONVERSATION_CHAT_COMPLETED
    //        | ChatEventType.CONVERSATION_CHAT_FAILED
    //        | ChatEventType.CONVERSATION_CHAT_REQUIRES_ACTION;
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
