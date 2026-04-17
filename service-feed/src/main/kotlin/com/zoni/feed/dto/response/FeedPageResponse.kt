package com.zoni.feed.dto.response

/** 피드 페이징 목록 응답 */
data class FeedPageResponse(
    val feeds: List<FeedSummaryResponse>,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val pageSize: Int,
    val isLast: Boolean
)

