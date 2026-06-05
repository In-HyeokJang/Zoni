package com.zoni.place.dto.response

import com.zoni.common.BenefitType
import com.zoni.common.PlaceDataSource
import java.time.LocalDateTime

/**
 * 장소 단건 응답 DTO
 *
 * 기존 카카오맵 기반 필드에 Phase 3-B 혜택 필드가 추가되었다.
 * 혜택 없는 일반 장소는 benefit 관련 필드가 모두 null 로 반환된다.
 */
data class PlaceResponse(
    val id: Long,
    val kakaoPlaceId: String,
    val name: String,
    val category: String,
    val address: String,
    val roadAddress: String,
    val phone: String?,
    val placeUrl: String?,
    val x: Double,
    val y: Double,
    val userId: Long,
    val nickname: String,
    val viewCount: Int,
    val createdAt: LocalDateTime,

    // ── Phase 3-B 혜택 필드 ────────────────────────────────────────────────
    val benefitType: BenefitType? = null,
    val discountInfo: String? = null,
    val targetAgeMin: Int? = null,
    val targetAgeMax: Int? = null,
    val dataSource: PlaceDataSource? = null,
    val sourceUrl: String? = null
)

data class PlacePageResponse(
    val places: List<PlaceResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val isLast: Boolean
)
