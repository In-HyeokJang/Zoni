package com.zoni.feed.dto.request

import jakarta.validation.constraints.Size

/**
 * 피드 수정 요청
 * null 이면 해당 필드 수정 안함 (부분 수정 지원)
 */
data class FeedUpdateRequest(
    @field:Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
    val title: String?,

    val content: String?
)

