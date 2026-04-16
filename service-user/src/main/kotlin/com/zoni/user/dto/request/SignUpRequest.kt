package com.zoni.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class SignUpRequest(
    @field:NotBlank(message = "이메일을 입력해주세요.")
    @field:Email(message = "이메일 형식이 올바르지 않습니다.")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요.")
    @field:Size(min = 4, message = "비밀번호는 4자 이상이어야 합니다.")
    val password: String,

    @field:NotBlank(message = "닉네임을 입력해주세요.")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 이어야 합니다.")
    val nickname: String
)
