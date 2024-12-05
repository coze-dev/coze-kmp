package com.coze.api.model

import com.coze.api.model.chat.CreateChatData
import com.coze.api.model.chat.StreamChatData
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int,
    val msg: String = "",
    val data: T,
    val error: ErrorData? = null
)

typealias CreateChatResponse = ApiResponse<CreateChatData>
typealias StreamChatResponse = ApiResponse<StreamChatData>
