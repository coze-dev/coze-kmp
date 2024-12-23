package com.coze.api.model

import io.ktor.http.Headers
import kotlinx.serialization.Serializable

open class CozeError(message: String) : Exception(message)

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

@Serializable
data class ErrorRes(
    val code: Int?,
    val msg: String?,
    val error: ErrorDetail?
)

@Serializable
data class ErrorDetail(
    val logid: String?,
    val detail: String?,
    val helpDoc: String?
)

class APIConnectionError(
    message: String? = "Connection error.",
    cause: Throwable? = null
) : APIError(null, null, message, null) {
    init {
        cause?.message
    }
}

class APIUserAbortError(
    message: String? = "Request was aborted."
) : APIError(null, null, message, null)

class BadRequestError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

class AuthenticationError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

class PermissionDeniedError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

class NotFoundError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

class RateLimitError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

class TimeoutError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

class InternalServerError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

class GatewayError(
    status: Int?,
    error: ErrorRes?,
    message: String?,
    headers: Headers?
) : APIError(status, error, message, headers)

fun castToError(err: Any?): Throwable {
    return if (err is Throwable) err else Exception(err.toString())
}

class JSONParseError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)