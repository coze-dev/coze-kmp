package com.coze.api.model

import io.ktor.http.Headers

open class CozeError(message: String) : Exception(message)

open class APIError(
    val status: Int?,
    val headers: Headers?,
    error: ErrorRes?,
    message: String?
) : CozeError(makeMessage(status, error, message, headers)) {

    val code = error?.code
    val msg = error?.msg
    val detail = error?.error?.detail
    val helpDoc = error?.error?.helpDoc
    val logid = headers?.get("x-tt-logid")
    val rawError = error

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
                val (code, msg, error) = errorBody
                code?.let { list.add("code: $it") }
                msg?.let { list.add("msg: $it") }
                if (error?.detail != null && msg != error.detail) {
                    list.add("detail: ${error.detail}")
                }
                val logId = error?.logid ?: headers?.get("x-tt-logid")
                logId?.let { list.add("logid: $it") }
                error?.helpDoc?.let { list.add("help doc: $it") }
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
            if (status == null) return APIConnectionError(errorResponse?.msg)

            val error = errorResponse
            return when {
                status == 400 || error?.code == 4000 -> BadRequestError(status, error, message, headers)
                status == 401 || error?.code == 4100 -> AuthenticationError(status, error, message, headers)
                status == 403 || error?.code == 4101 -> PermissionDeniedError(status, error, message, headers)
                status == 404 || error?.code == 4200 -> NotFoundError(status, error, message, headers)
                status == 429 || error?.code == 4013 -> RateLimitError(status, error, message, headers)
                status == 408 -> TimeoutError(status, error, message, headers)
                status == 502 -> GatewayError(status, error, message, headers)
                status >= 500 -> InternalServerError(status, error, message, headers)
                else -> APIError(status, headers, error, message)
            }
        }
    }
}

data class ErrorRes(
    val code: Int?,
    val msg: String?,
    val error: ErrorDetail?
)

data class ErrorDetail(
    val logid: String?,
    val detail: String?,
    val helpDoc: String?
)

fun castToError(err: Any?): Exception {
    return if (err is Exception) err else Exception(err.toString())
}

class APIConnectionError(
    message: String? = "Connection error.",
    cause: Throwable? = null
) : APIError(
    status = null,
    headers = null,
    error = null,
    message = message
) {
    init {
        // If you need to handle the cause later, add logic here
        // e.g., this.cause = cause
    }
}

class BadRequestError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)

class AuthenticationError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)

class PermissionDeniedError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)

class NotFoundError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)

class RateLimitError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)

class TimeoutError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)

class InternalServerError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)

class GatewayError(status: Int?, error: ErrorRes?, message: String?, headers: Headers?) :
    APIError(status, headers, error, message)