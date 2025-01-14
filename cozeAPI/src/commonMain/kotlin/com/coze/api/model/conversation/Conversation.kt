package com.coze.api.model.conversation

import com.coze.api.model.EnterMessage
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request parameters for creating a conversation
 */
@Serializable
data class CreateConversationReq(
    val messages: List<EnterMessage>? = null,
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null,
    @SerialName("bot_id")
    val botId: String? = null
)

/**
 * Request parameters for listing conversations
 */
@Serializable
data class ListConversationReq(
    @SerialName("bot_id")
    val botId: String,
    @SerialName("page_num")
    val pageNum: Int? = 1,
    @SerialName("page_size")
    val pageSize: Int? = 50
)

/**
 * Response data for listing conversations
 */
@Serializable
data class ListConversationsData(
    val conversations: List<Conversation>,
    @SerialName("has_more")
    val hasMore: Boolean
)

/**
 * Conversation information
 */
@Serializable
data class Conversation(
    /**
     * Conversation ID
     */
    val id: String,

    /**
     * Session creation time. The format is a 10-digit Unixtime timestamp in seconds.
     */
    @SerialName("created_at")
    val createdAt: Long,

    /**
     * Custom key-value pairs
     */
    @SerialName("meta_data")
    val metaData: Map<String, String>,

    /**
     * The section_id of the last message in the session
     */
    @SerialName("last_section_id")
    val lastSectionId: String? = null
)

/**
 * Conversation session information
 */
@Serializable
data class ConversationSession(
    val id: String,
    @SerialName("conversation_id")
    val conversationId: String
) 