package com.coze.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Message role type | 消息角色类型
 * Defines the role of message sender | 定义消息发送者的角色
 */
@Serializable
enum class RoleType {
    @SerialName("user")
    USER,
    @SerialName("assistant")
    ASSISTANT
}

/**
 * Message content type | 消息内容类型
 * Defines the type of message content | 定义消息内容的类型
 */
@Serializable
enum class ContentType {
    @SerialName("text")
    TEXT,
    @SerialName("image")
    IMAGE,
    @SerialName("audio")
    AUDIO,
    @SerialName("video")
    VIDEO,
    @SerialName("object_string")
    OBJECT_STRING,
    @SerialName("card")
    CARD
}

/**
 * Message type | 消息类型
 * Defines the type of message | 定义消息的类型
 */
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
    @SerialName("knowledge")
    KNOWLEDGE,
    @SerialName("verbose")
    VERBOSE
}

/**
 * Chat status | 聊天状态
 * Defines the status of a chat | 定义聊天的状态
 */
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

/**
 * Object string item for structured content | 结构化内容的对象字符串项
 * @property type Type of the item | 项目类型
 * @property text Text content | 文本内容
 */
@Serializable
data class ObjectStringItem(
    val type: String,
    val text: String
)

/**
 * Usage information | 使用信息
 * @property tokenCount Token count | 令牌数量
 * @property outputCount Output count | 输出数量
 * @property inputCount Input count | 输入数量
 */
@Serializable
data class Usage(
    @SerialName("token_count")
    val tokenCount: Int,
    @SerialName("output_count")
    val outputCount: Int,
    @SerialName("input_count")
    val inputCount: Int
)

/**
 * Simple message for request | 请求的简单消息
 * @property role Message role | 消息角色
 * @property content Message content | 消息内容
 * @property contentType Content type | 内容类型
 * @property metaData Additional metadata | 额外元数据
 * @property type Message type | 消息类型
 */
@Serializable
data class EnterMessage(
    val role: RoleType,
    val content: String? = null,
    @SerialName("content_type")
    var contentType: ContentType? = ContentType.TEXT,
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null,
    @SerialName("type")
    val type: String? = null
)

/**
 * Base message information | 基础消息信息
 * 
 * @property id Message ID, unique identifier | 消息ID，唯一标识符
 * @property conversationId Conversation ID | 对话ID
 * @property botId Bot ID that writes this message | 写入此消息的机器人ID
 * @property chatId Chat ID | 聊天ID
 * @property metaData Additional metadata | 额外元数据
 * @property role Message sender role | 消息发送者角色
 * @property content Message content | 消息内容
 * @property contentType Content type | 内容类型
 * @property createdAt Creation time (Unix timestamp) | 创建时间（Unix时间戳）
 * @property updatedAt Update time (Unix timestamp) | 更新时间（Unix时间戳）
 * @property type Message type | 消息类型
 * @property status Message status | 消息状态
 * @property usage Usage information | 使用信息
 * @property lastError Last error information | 最后的错误信息
 */
@Serializable
data class ChatV3Message(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String,
    @SerialName("bot_id")
    val botId: String? = null,
    @SerialName("chat_id")
    val chatId: String? = null,
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null,
    val role: RoleType,
    val content: String,
    @SerialName("content_type")
    val contentType: ContentType,
    @SerialName("created_at")
    val createdAt: Long? = null,
    @SerialName("updated_at")
    val updatedAt: Long? = null,
    val type: MessageType? = null,
    val status: String? = null,
    val usage: Usage? = null,
    @SerialName("last_error")
    val lastError: ErrorData? = ErrorData(0, "")
)
