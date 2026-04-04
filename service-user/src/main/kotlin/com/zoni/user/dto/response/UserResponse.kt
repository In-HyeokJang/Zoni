package com.zoni.user.dto.response

data class UserResponse(
    val userId: Long,
    val email: String,
    val nickname: String,
    val role: String
)

