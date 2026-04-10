package com.zoni.feed.dto.response

import java.time.LocalDateTime

/** 피드 목록용 요약 응답 (content 제외 → 트래픽 절약) */
data class FeedSummaryResponse(
    val id: Long,
    val userId: Long,
    val nickname: String,
    val title: String,
    val category: String,
    val placeId: Long?,       // 연관 장소 ID
    val imageUrl: String?,    // 대표 이미지 URL (썸네일)
    val viewCount: Int,
    val likeCount: Int,
    val createdAt: LocalDateTime
)
