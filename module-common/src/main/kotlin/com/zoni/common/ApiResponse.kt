package com.zoni.common

/**
 * 모든 API 응답의 공통 래퍼 클래스
 * 성공: { success: true, data: {...} }
 * 실패: { success: false, error: { code: "...", message: "..." } }
 */
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorResponse? = null
) {
    companion object {
        fun <T> ok(data: T): ApiResponse<T> =
            ApiResponse(success = true, data = data)

        fun fail(error: ErrorResponse): ApiResponse<Nothing> =
            ApiResponse(success = false, error = error)
    }

    data class ErrorResponse(
        val code: String,
        val message: String
    )
}
