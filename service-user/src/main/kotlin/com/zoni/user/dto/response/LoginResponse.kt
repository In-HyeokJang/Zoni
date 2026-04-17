package com.zoni.user.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val userId: Long,
    val email: String,
    val nickname: String,
    val profileImageUrl: String? = null,  // 카카오 로그인 시 프로필 이미지
    val oauthProvider: String? = null     // 로그인 방식 (null=이메일, KAKAO 등)
)