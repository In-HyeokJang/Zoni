package com.zoni.common

data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null,
    val code: String? = null
) {
    companion object {
        fun <T> ok(data: T) =
            ApiResponse(success = true, data = data)

        fun <T> ok() =
            ApiResponse<T>(success = true)

        fun error(code: String, message: String) =
            ApiResponse<Nothing>(success = false, code = code, message = message)
    }
}