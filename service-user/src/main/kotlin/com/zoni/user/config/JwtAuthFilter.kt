package com.zoni.user.config

import com.zoni.user.service.RefreshTokenService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

/**
 * JWT 인증 필터 (Spring MVC용)
 * 매 요청마다 1번만 실행됨 (OncePerRequestFilter)
 *
 * 동작 흐름:
 * 1. Authorization 헤더에서 "Bearer {token}" 추출
 * 2. JWT 유효성 검사
 * 3. 블랙리스트(로그아웃된 토큰) 여부 확인
 * 4. 유효하면 SecurityContext에 인증 정보 등록
 * 5. 다음 필터로 전달
 */
@Component
class JwtAuthFilter(
    private val jwtProvider: JwtProvider,
    private val refreshTokenService: RefreshTokenService
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val token = resolveToken(request)

        if (token != null
            && jwtProvider.isValid(token)
            && !refreshTokenService.isBlacklisted(token)   // 블랙리스트 체크
        ) {
            val email = jwtProvider.getEmail(token)
            val auth = UsernamePasswordAuthenticationToken(
                email,
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
