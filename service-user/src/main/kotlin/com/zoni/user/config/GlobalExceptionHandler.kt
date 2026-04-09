package com.zoni.user.config

import com.zoni.common.ApiResponse
import com.zoni.common.ZoniException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * 전역 예외 처리 핸들러
 * ZoniException → ApiResponse.fail 형태로 표준화해서 응답
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 입력값 검증 실패 처리 (@Valid 검증 실패 시)
     * 예: 빈 이메일, 이메일 형식 오류, 짧은 비밀번호 등
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ApiResponse<Nothing>> {
        // 첫 번째 에러 메시지만 반환 (여러 개면 첫 번째)
        val message = e.bindingResult.fieldErrors
            .firstOrNull()?.defaultMessage ?: "입력값이 올바르지 않습니다."
        log.warn("[ValidationException] {}", message)
        return ResponseEntity
            .status(400)
            .body(
                ApiResponse.fail(
                    ApiResponse.ErrorResponse(
                        code = "BAD_REQUEST",
                        message = message
                    )
                )
            )
    }

    /**
     * 비즈니스 예외 처리 (ZoniException)
     * 예: 중복 이메일, 유저 없음, 잘못된 비밀번호 등
     */
    @ExceptionHandler(ZoniException::class)
    fun handleZoniException(e: ZoniException): ResponseEntity<ApiResponse<Nothing>> {
        log.warn("[ZoniException] code={}, message={}", e.errorCode.name, e.message)
        return ResponseEntity
            .status(e.errorCode.httpStatus)
            .body(
                ApiResponse.fail(
                    ApiResponse.ErrorResponse(
                        code = e.errorCode.name,
                        message = e.errorCode.message
                    )
                )
            )
    }

    /**
     * 그 외 모든 예외 처리 (500 Internal Server Error)
     */
    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ApiResponse<Nothing>> {
        log.error("[UnhandledException] {}", e.message, e)
        return ResponseEntity
            .status(500)
            .body(
                ApiResponse.fail(
                    ApiResponse.ErrorResponse(
                        code = "INTERNAL_SERVER_ERROR",
                        message = "서버 오류가 발생했습니다."
                    )
                )
            )
    }
}
