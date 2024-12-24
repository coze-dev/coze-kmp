package com.coze.api.model

import kotlinx.serialization.Serializable

/**
 * Chat event types
 */
@Serializable
enum class ChatEventType(val value: String) {
    CONVERSATION_CHAT_CREATED("conversation.chat.created"),
    CONVERSATION_CHAT_IN_PROGRESS("conversation.chat.in_progress"),
    CONVERSATION_CHAT_COMPLETED("conversation.chat.completed"),
    CONVERSATION_CHAT_FAILED("conversation.chat.failed"),
    CONVERSATION_CHAT_REQUIRES_ACTION("conversation.chat.requires_action"),
    CONVERSATION_MESSAGE_DELTA("conversation.message.delta"),
    CONVERSATION_MESSAGE_COMPLETED("conversation.message.completed"),
    CONVERSATION_AUDIO_DELTA("conversation.audio.delta"),
    DONE("done"),
    ERROR("error");

    companion object {
        fun fromValue(value: String): ChatEventType? {
            return entries.find { it.value == value } ?: run {
                println("[Event] Unknown event type: $value")
                null
            }
        }
    }
} 