package com.zoni.common

/**
 * 전체 서비스 공통 에러 코드
 * httpStatus: HTTP 상태 코드
 * message: 사용자에게 보여줄 메시지
 */
enum class ErrorCode(
    val httpStatus: Int,
    val message: String
) {
    // ── User ──────────────────────────────────────────
    DUPLICATE_EMAIL(409, "이미 사용 중인 이메일입니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(401, "이메일 또는 비밀번호가 올바르지 않습니다."),

    // ── OAuth ─────────────────────────────────────────
    OAUTH_LOGIN_FAILED(401, "소셜 로그인에 실패했습니다."),
    OAUTH_EMAIL_REQUIRED(400, "소셜 계정에 이메일 정보가 필요합니다."),

    // ── JWT ───────────────────────────────────────────
    INVALID_TOKEN(401, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(401, "만료된 토큰입니다."),
    MISSING_TOKEN(401, "토큰이 존재하지 않습니다."),

    // ── Feed ──────────────────────────────────────────
    FEED_NOT_FOUND(404, "피드를 찾을 수 없습니다."),
    FEED_ALREADY_DELETED(400, "이미 삭제된 피드입니다."),

    // ── Place ─────────────────────────────────────────
    PLACE_NOT_FOUND(404, "장소를 찾을 수 없습니다."),
    PLACE_ALREADY_SAVED(409, "이미 저장한 장소입니다."),
    KAKAO_MAP_API_ERROR(502, "장소 검색 중 오류가 발생했습니다."),

    // ── Common ────────────────────────────────────────
    BAD_REQUEST(400, "잘못된 요청입니다."),
    FORBIDDEN(403, "접근 권한이 없습니다."),
    NOT_FOUND(404, "요청한 리소스를 찾을 수 없습니다."),
    INTERNAL_SERVER_ERROR(500, "서버 오류가 발생했습니다.")
}
