package com.zoni.user.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

/**
 * 출생 연도 업데이트 요청 DTO
 *
 * PATCH /api/users/me/birth-year — 회원가입 후 별도로 출생 연도를 등록·수정할 때 사용.
 * 청년 혜택 필터(Phase 3-B)에서 나이 기반 맞춤 혜택을 제공하기 위해 필요.
 */
data class BirthYearUpdateRequest(
    @field:NotNull(message = "출생 연도는 필수입니다.")
    @field:Min(value = 1900, message = "생년은 1900 이상이어야 합니다.")
    @field:Max(value = 2100, message = "생년 값이 올바르지 않습니다.")
    val birthYear: Int
)
