package com.zoni.notify.config

import com.zoni.common.ApiResponse
import com.zoni.common.ZoniException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(ZoniException::class)
    fun handleZoniException(e: ZoniException): ResponseEntity<ApiResponse<Nothing>> {
        val error = ApiResponse.ErrorResponse(code = e.errorCode.name, message = e.errorCode.message)
        return ResponseEntity.status(e.errorCode.httpStatus).body(ApiResponse.fail(error))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("[UnhandledException] {}", e.message, e)
        val error = ApiResponse.ErrorResponse(code = "INTERNAL_SERVER_ERROR", message = "서버 오류가 발생했습니다.")
        return ResponseEntity.status(500).body(ApiResponse.fail(error))
    }
}