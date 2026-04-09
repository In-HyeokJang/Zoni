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
    val viewCount: Int,
    val likeCount: Int,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

