package com.coze.api.model.workflow

import com.coze.api.model.EnterMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class RunWorkflowReq(
    @SerialName("workflow_id")
    val workflowId: String,
    @SerialName("bot_id")
    val botId: String? = null,
    val parameters: Map<String, JsonElement>? = null,
    val ext: Map<String, String>? = null,
    @SerialName("execute_mode")
    val executeMode: String? = null,
    @SerialName("connector_id")
    val connectorId: String? = null,
    @SerialName("app_id")
    val appId: String? = null,
    val stream: Boolean = false
)

@Serializable
data class RunWorkflowData(
    val data: String,
    val cost: String,
    val token: Int,
    @SerialName("debug_url")
    val debugUrl: String
)

@Serializable
data class ResumeWorkflowReq(
    @SerialName("workflow_id")
    val workflowId: String,
    @SerialName("event_id")
    val eventId: String,
    @SerialName("resume_data")
    val resumeData: String,
    @SerialName("interrupt_type")
    val interruptType: Int
)

@Serializable
data class ChatWorkflowReq(
    @SerialName("workflow_id")
    val workflowId: String,
    @SerialName("bot_id")
    val botId: String? = null,
    val parameters: Map<String, JsonElement>? = null,
    @SerialName("additional_messages")
    val additionalMessages: List<EnterMessage>,
    val ext: Map<String, String>? = null,
    @SerialName("app_id")
    val appId: String? = null,
)
