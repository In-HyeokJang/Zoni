package com.zoni.feed.dto.request

import com.zoni.feed.domain.FeedCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 피드 작성 요청
 * nickname은 JWT 토큰(JwtPrincipal)에서 꺼내므로 클라이언트가 보내지 않아도 됨
 */
data class FeedCreateRequest(
    @field:NotBlank(message = "제목을 입력해주세요.")
    @field:Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
    val title: String,

    @field:NotBlank(message = "내용을 입력해주세요.")
    val content: String,

    @field:NotNull(message = "카테고리를 선택해주세요.")
    val category: FeedCategory   // ROOM_WANTED | ROOM_OFFER | COMMUNITY | QNA
)

