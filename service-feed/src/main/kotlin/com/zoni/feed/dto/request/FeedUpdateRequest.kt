package com.zoni.feed.dto.request

/**
 * 피드 수정 요청
 * null 이면 해당 필드 수정 안함 (부분 수정 지원)
 */
data class FeedUpdateRequest(
    val title: String?,
    val content: String?
)

