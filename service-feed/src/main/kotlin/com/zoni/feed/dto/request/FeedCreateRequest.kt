package com.zoni.feed.dto.request

import com.zoni.feed.domain.FeedCategory
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size

/**
 * 피드 작성 요청
 * nickname은 JWT 토큰(JwtPrincipal)에서 꺼내므로 클라이언트가 보내지 않아도 됨
 * placeId, imageUrl은 선택 입력 (nullable)
 */
data class FeedCreateRequest(
    @field:NotBlank(message = "제목을 입력해주세요.")
    @field:Size(max = 200, message = "제목은 200자 이하로 입력해주세요.")
    val title: String,

    @field:NotBlank(message = "내용을 입력해주세요.")
    val content: String,

    @field:NotNull(message = "카테고리를 선택해주세요.")
    val category: FeedCategory,  // REVIEW | COURSE | PHOTO | COMMUNITY

    /** 연관 장소 ID (service-place와 연동, Phase 2) */
    val placeId: Long? = null,

    /** 대표 이미지 URL (Phase 2 이미지 업로드 구현 후 사용) */
    @field:Size(max = 500, message = "이미지 URL은 500자 이하여야 합니다.")
    val imageUrl: String? = null
)
