package com.coze.api.model.conversation

import com.coze.api.model.ContentType
import com.coze.api.model.ChatV3Message
import com.coze.api.model.RoleType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request parameters for creating a message
 */
@Serializable
data class CreateMessageReq(
    val role: RoleType,
    val content: String,
    @SerialName("content_type")
    val contentType: ContentType,
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null
)

/**
 * Request parameters for updating a message
 */
@Serializable
data class UpdateMessageReq(
    @SerialName("meta_data")
    val metaData: Map<String, String>? = null,
    val content: String? = null,
    @SerialName("content_type")
    val contentType: ContentType? = null
)

/**
 * Request parameters for listing messages
 */
@Serializable
data class ListMessageReq(
    val order: String? = null,
    @SerialName("chat_id")
    val chatId: String? = null,
    @SerialName("before_id")
    val beforeId: String? = null,
    @SerialName("after_id")
    val afterId: String? = null,
    val limit: Int? = null
)

/**
 * Response data for listing messages
 */
@Serializable
data class ListMessageData(
    val data: List<ChatV3Message>,
    @SerialName("first_id")
    val firstId: String,
    @SerialName("last_id")
    val lastId: String,
    @SerialName("has_more")
    val hasMore: Boolean,
    val code: Int
)