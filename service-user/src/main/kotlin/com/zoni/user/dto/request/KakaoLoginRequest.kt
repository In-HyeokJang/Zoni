package com.zoni.user.dto.request

import jakarta.validation.constraints.NotBlank

/** 카카오 로그인 요청 - 프론트에서 카카오 인가 코드 받아서 전달 */
data class KakaoLoginRequest(
    @field:NotBlank(message = "인가 코드를 입력해주세요.")
    val code: String
)

