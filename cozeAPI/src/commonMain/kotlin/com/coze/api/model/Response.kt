package com.coze.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ApiResponse<T>(
    val code: Int = 0,
    val msg: String = "",
    val data: T? = null,
    @SerialName("request_id")
    val requestId: String? = null,
    val detail: Map<String, String>? = null,
    // OAuth2 错误格式
    val error: String? = null,
    @SerialName("error_code")
    val errorCode: String? = null,
    @SerialName("error_message")
    val errorMessage: String? = null,
    val total: Int? = 0,
)
