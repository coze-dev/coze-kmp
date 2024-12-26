package com.coze.api.model

import com.coze.api.model.chat.CreateChatData
import io.ktor.sse.ServerSentEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Event types for all events (chat, workflow, etc.)
 */
@Serializable
enum class EventType(val value: String) {
    // Chat events
    CONVERSATION_CHAT_CREATED("conversation.chat.created"),
    CONVERSATION_CHAT_IN_PROGRESS("conversation.chat.in_progress"),
    CONVERSATION_CHAT_COMPLETED("conversation.chat.completed"),
    CONVERSATION_CHAT_FAILED("conversation.chat.failed"),
    CONVERSATION_CHAT_REQUIRES_ACTION("conversation.chat.requires_action"),
    CONVERSATION_MESSAGE_DELTA("conversation.message.delta"),
    CONVERSATION_MESSAGE_COMPLETED("conversation.message.completed"),
    CONVERSATION_AUDIO_DELTA("conversation.audio.delta"),

    // Common events
    DONE("done"),
    ERROR("error"),

    // Workflow events
    MESSAGE("Message"),
    WORKFLOW_ERROR("Error"),
    WORKFLOW_DONE("Done"),
    INTERRUPT("Interrupt");

    companion object {
        fun fromValue(value: String): EventType? {
            return entries.find { it.value == value }
        }
    }
}

@Serializable
sealed class BaseEventMessage {
    abstract val content: String
    @SerialName("node_is_finish")
    abstract val nodeIsFinish: Boolean
    @SerialName("node_seq_id")
    abstract val nodeSeqId: String
    @SerialName("node_title")
    abstract val nodeTitle: String
    @SerialName("content_type")
    abstract val contentType: String?
    abstract val cost: String?
    abstract val token: Int?
}

@Serializable
sealed class BaseEventError {
    @SerialName("error_code")
    abstract val errorCode: Int
    @SerialName("error_message")
    abstract val errorMessage: String
}

@Serializable
sealed class BaseEventInterruptData {
    abstract val data: String
    @SerialName("event_id")
    abstract val eventId: String
    abstract val type: Int
}

@Serializable
sealed class BaseEventInterrupt {
    @SerialName("interrupt_data")
    abstract val interruptData: BaseEventInterruptData
    @SerialName("node_title")
    abstract val nodeTitle: String
}

@Serializable
sealed class BaseEventDone {
    @SerialName("debug_url")
    abstract val debugUrl: String
}

sealed class BaseStreamData {
    abstract val event: EventType
}

fun ServerSentEvent.toEventType(): EventType? {
    return event?.let { EventType.fromValue(it) }
}

@Serializable
sealed class WorkflowStreamData : BaseStreamData() {
    data class MessageEvent(val data: WorkflowEventMessage) : WorkflowStreamData() {
        override val event = EventType.MESSAGE
    }
    data class ErrorEvent(val data: WorkflowEventError) : WorkflowStreamData() {
        override val event = EventType.WORKFLOW_ERROR
    }
    data class InterruptEvent(val data: WorkflowEventInterrupt) : WorkflowStreamData() {
        override val event = EventType.INTERRUPT
    }
    data class DoneEvent(val data: WorkflowEventDone) : WorkflowStreamData() {
        override val event = EventType.WORKFLOW_DONE
    }
}

@Serializable
data class WorkflowEventMessage(
    override val content: String,
    @SerialName("node_is_finish") override val nodeIsFinish: Boolean = false,
    @SerialName("node_seq_id") override val nodeSeqId: String = "0",
    @SerialName("node_title") override val nodeTitle: String = "",
    @SerialName("content_type") override val contentType: String? = null,
    override val cost: String? = null,
    override val token: Int? = null
) : BaseEventMessage()

@Serializable
data class WorkflowEventError(
    @SerialName("error_code") override val errorCode: Int,
    @SerialName("error_message") override val errorMessage: String
) : BaseEventError()

@Serializable
data class WorkflowEventInterruptData(
    override val data: String = "",
    @SerialName("event_id") override val eventId: String,
    override val type: Int
) : BaseEventInterruptData()

@Serializable
data class WorkflowEventInterrupt(
    @SerialName("interrupt_data") override val interruptData: WorkflowEventInterruptData,
    @SerialName("node_title") override val nodeTitle: String
) : BaseEventInterrupt()

@Serializable
data class WorkflowEventDone(
    @SerialName("debug_url") override val debugUrl: String
) : BaseEventDone()

fun sseEvent2WorkflowData(event: ServerSentEvent): WorkflowStreamData {
    val eventData = event.data ?: throw IllegalArgumentException("Event data is null")
    val eventType = event.toEventType() ?: throw IllegalArgumentException("Unknown event type: ${event.event}")

    return when (eventType) {
        EventType.MESSAGE -> {
            val data = Json.decodeFromString<WorkflowEventMessage>(eventData)
            WorkflowStreamData.MessageEvent(data)
        }
        EventType.WORKFLOW_ERROR -> {
            val data = Json.decodeFromString<WorkflowEventError>(eventData)
            WorkflowStreamData.ErrorEvent(data)
        }
        EventType.INTERRUPT -> {
            val data = Json.decodeFromString<WorkflowEventInterrupt>(eventData)
            WorkflowStreamData.InterruptEvent(data)
        }
        EventType.WORKFLOW_DONE -> {
            val data = Json.decodeFromString<WorkflowEventDone>(eventData)
            WorkflowStreamData.DoneEvent(data)
        }
        else -> throw IllegalArgumentException("Unexpected event type for workflow: $eventType")
    }
}

@Serializable
sealed class StreamChatData : BaseStreamData() {
    data class CreateChatEvent(
        // event:
        // | EventType.CONVERSATION_CHAT_CREATED
        // | EventType.CONVERSATION_CHAT_IN_PROGRESS
        // | EventType.CONVERSATION_CHAT_COMPLETED
        // | EventType.CONVERSATION_CHAT_FAILED
        // | EventType.CONVERSATION_CHAT_REQUIRES_ACTION;
        override val event: EventType,
        val data: CreateChatData
    ) : StreamChatData()

    data class ChatMessageEvent(
        // event:
        //        | EventType.CONVERSATION_MESSAGE_DELTA
        //        | EventType.CONVERSATION_MESSAGE_COMPLETED
        //        | EventType.CONVERSATION_AUDIO_DELTA;
        override val event: EventType,
        val data: ChatV3Message
    ) : StreamChatData()

    data class DoneEvent(
        override val event: EventType = EventType.DONE,
        val data: String = "[DONE]"
    ) : StreamChatData()

    data class ErrorEvent(
        override val event: EventType = EventType.ERROR,
        val data: ErrorData
    ) : StreamChatData()
}

fun sseEvent2ChatData(event: ServerSentEvent): StreamChatData {
    val eventType = event.toEventType() ?: throw IllegalArgumentException("Unknown event type: ${event.event}")
    val jsonFormat = Json { ignoreUnknownKeys = true }

    return when (eventType) {
        EventType.ERROR -> {
            val errorData = jsonFormat.decodeFromString<ErrorData>(event.data ?: "")
            StreamChatData.ErrorEvent(data = errorData)
        }
        EventType.CONVERSATION_MESSAGE_DELTA,
        EventType.CONVERSATION_MESSAGE_COMPLETED,
        EventType.CONVERSATION_AUDIO_DELTA -> {
            val message = jsonFormat.decodeFromString<ChatV3Message>(event.data ?: "")
            StreamChatData.ChatMessageEvent(eventType, message)
        }
        EventType.CONVERSATION_CHAT_CREATED,
        EventType.CONVERSATION_CHAT_IN_PROGRESS,
        EventType.CONVERSATION_CHAT_COMPLETED,
        EventType.CONVERSATION_CHAT_FAILED,
        EventType.CONVERSATION_CHAT_REQUIRES_ACTION -> {
            val chatData = jsonFormat.decodeFromString<CreateChatData>(event.data ?: "")
            StreamChatData.CreateChatEvent(eventType, chatData)
        }
        EventType.DONE -> StreamChatData.DoneEvent()
        else -> throw IllegalArgumentException("Unsupported event type: $eventType")
    }
}