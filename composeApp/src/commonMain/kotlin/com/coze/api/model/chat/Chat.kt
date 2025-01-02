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
    val botId: String?=null,
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
