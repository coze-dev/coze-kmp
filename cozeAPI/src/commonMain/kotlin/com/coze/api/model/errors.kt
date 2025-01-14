package com.coze.api.model

import io.ktor.http.Headers
import kotlinx.serialization.Serializable

/**
 * Base Coze error | Coze基础错误
 * @param message Error message | 错误消息
 */
open class CozeError(message: String) : Exception(message)

/**
 * API error | API错误
 * @param status HTTP status code | HTTP状态码
 * @param error Error response | 错误响应
 * @param message Error message | 错误消息
 * @param headers HTTP headers | HTTP头部
 */
open class APIError(
    val status: Int?,
    val error: ErrorRes?,
    message: String?,
    headers: Headers? = null,
) : CozeError(makeMessage(status, error, message, headers)) {

    val code: Int? = error?.code
    val msg: String? = error?.msg
    val detail: String? = error?.error?.detail
    val helpDoc: String? = error?.error?.helpDoc
    val logid: String? = headers?.get("x-tt-logid")
    val rawError: ErrorRes? = error

    companion object {
        /**
         * Create error message | 创建错误消息
         */
        private fun makeMessage(
            status: Int?,
            errorBody: ErrorRes?,
            message: String?,
            headers: Headers?
        ): String {
            if (errorBody == null && message != null) return message
            if (errorBody != null) {
                val list = mutableListOf<String>()
                errorBody.code?.let { list.add("code: $it") }
                errorBody.msg?.let { list.add("msg: $it") }
                if (errorBody.error?.detail != null && errorBody.msg != errorBody.error.detail) {
                    list.add("detail: ${errorBody.error.detail}")
                }
                val logId = errorBody.error?.logid ?: headers?.get("x-tt-logid")
                logId?.let { list.add("logid: $it") }
                errorBody.error?.helpDoc?.let { list.add("help doc: $it") }
                return list.joinToString(", ")
            }
            return status?.let { "http status code: $it (no body)" } ?: "(no status code or body)"
        }

        /**
         * Generate specific API error | 生成特定的API错误
         */
        fun generate(
            status: Int?,
            errorResponse: ErrorRes?,
            message: String?,
            headers: Headers?
        ): APIError {
            if (status == null) {
                return APIConnectionError(cause = castToError(errorResponse))
            }

            return when {
                status == 400 || errorResponse?.code == 4000 -> BadRequestError(status, errorResponse, message, headers)
                status == 401 || errorResponse?.code == 4100 -> AuthenticationError(status, errorResponse, message, headers)
                status == 403 || errorResponse?.code == 4101 -> PermissionDeniedError(status, errorResponse, message, headers)
                status == 404 || errorResponse?.code == 4200 -> NotFoundError(status, errorResponse, message, headers)
                status == 429 || errorResponse?.code == 4013 -> RateLimitError(status, errorResponse, message, headers)
                status == 408 -> TimeoutError(status, errorResponse, message, headers)
                status == 502 -> GatewayError(status, errorResponse, message, headers)
                status >= 500 -> InternalServerError(status, errorResponse, message, headers)
                else -> APIError(status, errorResponse, message, headers)
            }
        }
    }
}

/**
 * Error response | 错误响应
 * @property code Error code | 错误代码
 * @property msg Error message | 错误消息
 * @property error Error details | 错误详情
 */
@Serializable
data class ErrorRes(
    val code: Int?,
    val msg: String?,
    val error: ErrorDetail?
)

/**
 * Error detail | 错误详情
 * @property logid Log ID | 日志ID
 * @property detail Error detail | 错误详情
 * @property helpDoc Help documentation | 帮助文档
 */
@Serializable
data class ErrorDetail(
    val logid: String?,
    val detail: String?,
    val helpDoc: String?
)

/**
 * API connection error | API连接错误
 */
class APIConnectionError(
    message: String? = "Connection error.",
    cause: Throwable? = null
) : APIError(null, null, message, null) {
    init {
        cause?.message
    }
}

/**
 * API user abort error | API用户中止错误
 */
class APIUserAbortError(
    message: String? = "Request was aborted."
) : APIError(null, null, message, null)

/**
 * Bad request error | 错误请求错误
 */
class BadRequestError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Authentication error | 认证错误
 */
class AuthenticationError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Permission denied error | 权限拒绝错误
 */
class PermissionDeniedError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Not found error | 未找到错误
 */
class NotFoundError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Rate limit error | 速率限制错误
 */
class RateLimitError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Timeout error | 超时错误
 */
class TimeoutError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Internal server error | 内部服务器错误
 */
class InternalServerError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Gateway error | 网关错误
 */
class GatewayError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

/**
 * Cast any object to error | 将任意对象转换为错误
 */
fun castToError(err: Any?): Throwable {
    return if (err is Throwable) err else Exception(err.toString())
}

/**
 * JSON parse error | JSON解析错误
 */
class JSONParseError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Error data | 错误数据
 * @property code Error code | 错误代码
 * @property msg Error message | 错误消息
 */
@Serializable
data class ErrorData(
    val code: Int,
    val msg: String
)