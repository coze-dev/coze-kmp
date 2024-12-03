package com.coze.api.model.chat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
data class Message(
    val role: String,
    val content: String,
    @SerialName("content_type")
    var contentType: String = "text"
)

@Serializable
data class ChatResponse(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
)
