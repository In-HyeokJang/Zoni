package com.zoni.user.dto.response

data class UserResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val role: String,
    val profileImageUrl: String?,   // 카카오 프로필 이미지 (OAuth 로그인 시)
    val oauthProvider: String?      // 로그인 방식 (null=이메일, KAKAO 등)
)
