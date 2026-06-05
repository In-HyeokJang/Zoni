package com.zoni.user.dto.request

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
    val nickname: String,

    /**
     * 출생 연도 (선택 입력 — Phase 3-B 청년 혜택 연령 필터용)
     * 미입력 시 null 로 저장되며, 이후 PATCH /api/users/me/birth-year 로 업데이트 가능.
     */
    @field:Min(value = 1900, message = "생년은 1900 이상이어야 합니다.")
    @field:Max(value = 2100, message = "생년 값이 올바르지 않습니다.")
    val birthYear: Int? = null
)
