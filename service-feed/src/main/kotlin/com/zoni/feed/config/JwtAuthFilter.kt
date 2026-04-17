package com.zoni.feed.config

import com.zoni.common.JwtPrincipal
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * service-feed JWT 인증 필터
 *
 * service-user와 달리 JwtPrincipal(userId, email, nickname)을 꺼내서 등록.
 * → FeedController에서 @AuthenticationPrincipal JwtPrincipal 로 바로 사용 가능
 *
 * 블랙리스트 확인: service-user에서 로그아웃한 토큰은 Redis blacklist에 등록되므로
 * 동일한 Redis를 바라보며 차단한다.
 */
@Component
class JwtAuthFilter(
    private val jwtProvider: JwtProvider,
    private val tokenBlacklistService: TokenBlacklistService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null && jwtProvider.isValid(token) && !tokenBlacklistService.isBlacklisted(token)) {
            val principal = JwtPrincipal(
                userId   = jwtProvider.getUserId(token),
                email    = jwtProvider.getEmail(token),
                nickname = jwtProvider.getNickname(token)
            )

            val auth = UsernamePasswordAuthenticationToken(
                principal,
                null,
                listOf(SimpleGrantedAuthority("ROLE_USER"))
            )
            SecurityContextHolder.getContext().authentication = auth
        }

        filterChain.doFilter(request, response)
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearer = request.getHeader("Authorization") ?: return null
        if (!bearer.startsWith("Bearer ")) return null
        return bearer.substring(7)
    }
}

