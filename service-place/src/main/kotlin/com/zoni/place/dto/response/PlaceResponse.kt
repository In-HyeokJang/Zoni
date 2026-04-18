package com.zoni.place.dto.response

import java.time.LocalDateTime

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
    val createdAt: LocalDateTime
)

data class PlacePageResponse(
    val places: List<PlaceResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val isLast: Boolean
)