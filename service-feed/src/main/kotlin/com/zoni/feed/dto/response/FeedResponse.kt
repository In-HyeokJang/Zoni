package com.zoni.feed.dto.response

import java.time.LocalDateTime

/** 피드 상세 응답 (단건 조회) */
data class FeedResponse(
    val id: Long,
    val userId: Long,
    val nickname: String,
    val title: String,
    val content: String,
    val category: String,
    val placeId: Long?,       // 연관 장소 ID (Phase 2 연동)
    val imageUrl: String?,    // 대표 이미지 URL
    val viewCount: Int,
    val likeCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
