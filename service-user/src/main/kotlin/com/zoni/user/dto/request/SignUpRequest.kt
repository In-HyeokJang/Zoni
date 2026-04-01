package com.zoni.user.dto.request

data class SignUpRequest(
    val email: String,
    val password: String,
    val nickname: String
)