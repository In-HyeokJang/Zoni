package com.zoni.feed.dto.request

import com.zoni.feed.domain.FeedCategory

/**
 * 피드 작성 요청
 * nickname은 JWT 토큰(JwtPrincipal)에서 꺼내므로 클라이언트가 보내지 않아도 됨
 */
data class FeedCreateRequest(
    val title: String,
    val content: String,
    val category: FeedCategory   // ROOM_WANTED | ROOM_OFFER | COMMUNITY | QNA
)

