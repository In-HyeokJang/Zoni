package com.zoni.place.dto.request

import com.zoni.common.BenefitType
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 혜택 장소 직접 등록 요청 DTO
 *
 * POST /api/places/benefit — 사용자가 "이 장소는 청년 혜택이 있어요"라고 직접 신고/등록할 때 사용.
 * benefitType, discountInfo 는 필수이며, 연령 범위는 선택 사항.
 */
data class BenefitSaveRequest(

    @field:NotBlank(message = "카카오 장소 ID는 필수입니다")
    val kakaoPlaceId: String,

    @field:NotBlank(message = "장소명은 필수입니다")
    val name: String,

    @field:NotBlank(message = "카테고리는 필수입니다")
    val category: String,

    @field:NotBlank(message = "주소는 필수입니다")
    val address: String,

    val roadAddress: String = "",
    val phone: String? = null,
    val placeUrl: String? = null,

    @field:NotNull(message = "경도(x)는 필수입니다")
    val x: Double,

    @field:NotNull(message = "위도(y)는 필수입니다")
    val y: Double,

    // ── 혜택 정보 (필수) ──────────────────────────────────────────────────

    @field:NotNull(message = "혜택 유형은 필수입니다")
    val benefitType: BenefitType,

    @field:NotBlank(message = "혜택 내용은 필수입니다")
    @field:Size(max = 300, message = "혜택 내용은 300자 이내여야 합니다")
    val discountInfo: String,

    /** 혜택 대상 연령 하한 (null = 제한 없음) */
    @field:Min(value = 1, message = "최소 연령은 1 이상이어야 합니다")
    @field:Max(value = 100, message = "최소 연령은 100 이하여야 합니다")
    val targetAgeMin: Int? = null,

    /** 혜택 대상 연령 상한 (null = 제한 없음) */
    @field:Min(value = 1, message = "최대 연령은 1 이상이어야 합니다")
    @field:Max(value = 100, message = "최대 연령은 100 이하여야 합니다")
    val targetAgeMax: Int? = null,

    /** 혜택 출처 URL (공식 사이트, 카드사 이벤트 페이지 등) */
    val sourceUrl: String? = null
)
