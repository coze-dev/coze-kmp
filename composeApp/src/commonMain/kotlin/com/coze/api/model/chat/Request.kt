package com.coze.api.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequest(
    @SerialName("bot_id")
    val botId: String,
    @SerialName("user_id")
    var userId: String? = null,
    @SerialName("additional_messages")
    val additionalMessages: List<Message>? = null,
    @SerialName("custom_variables")
    val customVariables: Map<String, String>? = null,
    @SerialName("auto_save_history")
    val autoSaveHistory: Boolean? = true,
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null,
    @SerialName("conversation_id")
    val conversationId: String? = null,
    @SerialName("extra_params")
    val extraParams: List<String>? = null,

    val stream: Boolean? = false
)

typealias CreateChatReq = ChatRequest
typealias StreamChatReq = ChatRequest

@Serializable
data class SubmitToolOutputsReq(
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("chat_id")
    val chatId: String,
    @SerialName("tool_outputs")
    val toolOutputs: List<ToolOutputType>,
    val stream: Boolean
)

@Serializable
data class RetrieveChatReq(
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("chat_id")
    val chatId: String
)

@Serializable
data class ToolOutputType(
    @SerialName("tool_call_id")
    val toolCallId: String,
    val output: String
)
