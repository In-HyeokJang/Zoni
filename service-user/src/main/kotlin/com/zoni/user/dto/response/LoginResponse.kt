package com.zoni.user.dto.response

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer",
    val userId: Long,
    val email: String,
    val nickname: String
)