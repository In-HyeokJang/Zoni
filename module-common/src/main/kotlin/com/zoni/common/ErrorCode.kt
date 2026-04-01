package com.zoni.common

enum class ErrorCode(val code: String, val message: String) {
    // 공통
    INTERNAL_ERROR("COMMON_001", "서버 내부 오류"),
    INVALID_INPUT("COMMON_002", "잘못된 입력값"),
    NOT_FOUND("COMMON_003", "리소스를 찾을 수 없음"),

    // 인증
    UNAUTHORIZED("AUTH_001", "인증이 필요합니다"),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰"),
    EXPIRED_TOKEN("AUTH_003", "만료된 토큰"),

    // 유저
    USER_NOT_FOUND("USER_001", "존재하지 않는 사용자"),
    DUPLICATE_EMAIL("USER_002", "이미 사용 중인 이메일")
}