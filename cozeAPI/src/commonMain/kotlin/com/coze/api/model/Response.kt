package com.coze.api.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * API Response | API响应
 * @param T Type of response data | 响应数据的类型
 * @property code Response code | 响应代码
 * @property msg Response message | 响应消息
 * @property data Response data | 响应数据
 * @property requestId Request ID | 请求ID
 * @property detail Additional details | 额外详情
 * @property error OAuth2 error | OAuth2错误
 * @property errorCode Error code | 错误代码
 * @property errorMessage Error message | 错误消息
 * @property total Total count | 总数
 */
@Serializable
data class ApiResponse<T>(
    val code: Int = 0,
    val msg: String = "",
    val data: T? = null,
    @SerialName("request_id")
    val requestId: String? = null,
    val detail: Map<String, String>? = null,
    // OAuth2 error format | OAuth2 错误格式
    val error: String? = null,
    @SerialName("error_code")
    val errorCode: String? = null,
    @SerialName("error_message")
    val errorMessage: String? = null,
    val total: Int? = 0,
)
