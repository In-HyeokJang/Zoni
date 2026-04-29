package com.zoni.feed.dto.response

import java.time.LocalDateTime

data class CommentResponse(
    val id: Long,
    val feedId: Long,
    val userId: Long,
    val nickname: String,
    val content: String,
    val createdAt: LocalDateTime,
    val isMine: Boolean // 클라이언트에서 삭제 버튼 노출 여부 판단을 위해 추가
)
