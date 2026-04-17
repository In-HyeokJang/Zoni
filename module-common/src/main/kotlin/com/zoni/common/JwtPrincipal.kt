package com.zoni.common

/**
 * JWT에서 추출한 인증 주체 정보
 *
 * JwtAuthFilter에서 SecurityContext에 등록되며,
 * 각 서비스 컨트롤러에서 @AuthenticationPrincipal 로 꺼내 사용한다.
 *
 * 사용 예:
 *   @PostMapping
 *   fun create(@AuthenticationPrincipal principal: JwtPrincipal, ...)
 */
data class JwtPrincipal(
    val userId: Long,
    val email: String,
    val nickname: String
)

