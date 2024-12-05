package com.coze.api.model

import kotlinx.serialization.Serializable

@Serializable
data class ErrorData(
    val code: Int,
    val msg: String
)

// 定义 ChatEventType 枚举
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
            return entries.find { it.value == value }?: run {
            // 处理未知事件类型
            println("未知事件类型: $value")
            null
        }
        }
    }
}
