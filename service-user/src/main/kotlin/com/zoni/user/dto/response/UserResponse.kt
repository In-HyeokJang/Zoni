package com.zoni.user.dto.response

/**
 * 사용자 정보 응답 DTO
 *
 * GET /api/users/me 응답 형식.
 * birthYear 는 Phase 3-B 청년 혜택 연령 필터를 위해 추가되었으며, 미입력 시 null.
 */
data class UserResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val role: String,
    val profileImageUrl: String?,   // 카카오 프로필 이미지 (OAuth 로그인 시)
    val oauthProvider: String?,     // 로그인 방식 (null=이메일, KAKAO 등)
    /** 출생 연도 (Phase 3-B 청년 혜택 필터용, 미입력 시 null) */
    val birthYear: Int? = null
)
