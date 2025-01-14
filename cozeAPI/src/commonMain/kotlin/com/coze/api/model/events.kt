package com.coze.api.model

import com.coze.api.model.chat.CreateChatData
import io.ktor.sse.ServerSentEvent
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Event types for all events | 所有事件的类型
 * Includes chat events, workflow events, and common events | 包括聊天事件、工作流事件和通用事件
 */
@Serializable
enum class EventType(val value: String) {
    // Chat events | 聊天事件
    CONVERSATION_CHAT_CREATED("conversation.chat.created"),
    CONVERSATION_CHAT_IN_PROGRESS("conversation.chat.in_progress"),
    CONVERSATION_CHAT_COMPLETED("conversation.chat.completed"),
    CONVERSATION_CHAT_FAILED("conversation.chat.failed"),
    CONVERSATION_CHAT_REQUIRES_ACTION("conversation.chat.requires_action"),
    CONVERSATION_MESSAGE_DELTA("conversation.message.delta"),
    CONVERSATION_MESSAGE_COMPLETED("conversation.message.completed"),
    CONVERSATION_AUDIO_DELTA("conversation.audio.delta"),

    // Common events | 通用事件
    DONE("done"),
    ERROR("error"),

    // Workflow events | 工作流事件
    MESSAGE("Message"),
    WORKFLOW_ERROR("Error"),
    WORKFLOW_DONE("Done"),
    INTERRUPT("Interrupt");

    companion object {
        /**
         * Get event type from string value | 从字符串值获取事件类型
         * @param value String value | 字符串值
         * @return EventType? Event type if found | 找到的事件类型
         */
        fun fromValue(value: String): EventType? {
            return entries.find { it.value == value }
        }
    }
}

/**
 * Base event message | 基础事件消息
 */
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

/**
 * Base event error | 基础事件错误
 */
@Serializable
sealed class BaseEventError {
    @SerialName("error_code")
    abstract val errorCode: Int
    @SerialName("error_message")
    abstract val errorMessage: String
}

/**
 * Base event interrupt data | 基础事件中断数据
 */
@Serializable
sealed class BaseEventInterruptData {
    abstract val data: String
    @SerialName("event_id")
    abstract val eventId: String
    abstract val type: Int
}

/**
 * Base event interrupt | 基础事件中断
 */
@Serializable
sealed class BaseEventInterrupt {
    @SerialName("interrupt_data")
    abstract val interruptData: BaseEventInterruptData
    @SerialName("node_title")
    abstract val nodeTitle: String
}

/**
 * Base event done | 基础事件完成
 */
@Serializable
sealed class BaseEventDone {
    @SerialName("debug_url")
    abstract val debugUrl: String
}

/**
 * Base stream data | 基础流数据
 */
sealed class BaseStreamData {
    abstract val event: EventType
}

/**
 * Convert ServerSentEvent to EventType | 将ServerSentEvent转换为EventType
 */
fun ServerSentEvent.toEventType(): EventType? {
    return event?.let { EventType.fromValue(it) }
}

/**
 * Workflow stream data | 工作流流数据
 */
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
    data class CommonErrorEvent(val data: ErrorData) : WorkflowStreamData() {
        override val event = EventType.ERROR
    }
}

/**
 * Workflow event message | 工作流事件消息
 */
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

/**
 * Workflow event error | 工作流事件错误
 */
@Serializable
data class WorkflowEventError(
    @SerialName("error_code") override val errorCode: Int,
    @SerialName("error_message") override val errorMessage: String
) : BaseEventError()

/**
 * Workflow event interrupt data | 工作流事件中断数据
 */
@Serializable
data class WorkflowEventInterruptData(
    override val data: String = "",
    @SerialName("event_id") override val eventId: String,
    override val type: Int
) : BaseEventInterruptData()

/**
 * Workflow event interrupt | 工作流事件中断
 */
@Serializable
data class WorkflowEventInterrupt(
    @SerialName("interrupt_data") override val interruptData: WorkflowEventInterruptData,
    @SerialName("node_title") override val nodeTitle: String
) : BaseEventInterrupt()

/**
 * Workflow event done | 工作流事件完成
 */
@Serializable
data class WorkflowEventDone(
    @SerialName("debug_url") override val debugUrl: String
) : BaseEventDone()

/**
 * Convert SSE event to workflow data | 将SSE事件转换为工作流数据
 */
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
        EventType.ERROR -> {
            val data = Json.decodeFromString<ErrorData>(eventData)
            WorkflowStreamData.CommonErrorEvent(data)
        }
        else -> throw IllegalArgumentException("Unexpected event type for workflow: $eventType")
    }
}

/**
 * Stream chat data | 流式聊天数据
 */
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

/**
 * Convert SSE event to chat data | 将SSE事件转换为聊天数据
 */
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

/**
 * Chat flow data | 聊天流数据
 */
@Serializable
sealed class ChatFlowData : BaseStreamData() {
    data class WorkflowEvent(val data: WorkflowStreamData) : ChatFlowData() {
        override val event = data.event
    }
    
    data class ChatEvent(val data: StreamChatData) : ChatFlowData() {
        override val event = data.event
    }
}

/**
 * Convert chat data to flow data | 将聊天数据转换为流数据
 */
fun chatData2FlowData(chatData: StreamChatData): ChatFlowData {
    return ChatFlowData.ChatEvent(chatData)
}

fun workflowData2FlowData(workflowData: WorkflowStreamData): ChatFlowData {
    return ChatFlowData.WorkflowEvent(workflowData)
}

fun sseEvent2ChatFlowData(event: ServerSentEvent): ChatFlowData {
    return try {
        workflowData2FlowData(sseEvent2WorkflowData(event))
    } catch (e: IllegalArgumentException) {
        chatData2FlowData(sseEvent2ChatData(event))
    }
}