package com.coze.api.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class ChatRequest(
    @SerialName("bot_id")
    val botId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("additional_messages")
    val additionalMessages: List<Message> = emptyList()
)