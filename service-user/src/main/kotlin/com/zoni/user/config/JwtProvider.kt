package com.zoni.user.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Date

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String,
    @Value("\${jwt.access-token-expire}") private val expiration: Long,
    @Value("\${jwt.refresh-token-expire}") private val refreshExpiration: Long
) {
    private val key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun generateToken(userId: Long, email: String, nickname: String): String {
        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("nickname", nickname)   // ← service-feed 등 다른 서비스에서 사용
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + expiration))
            .signWith(key)
            .compact()
    }

    /** Refresh Token 생성 (7일, Redis에 저장해서 관리) */
    fun generateRefreshToken(userId: Long, email: String, nickname: String): String {
        return Jwts.builder()
            .subject(email)
            .claim("userId", userId)
            .claim("nickname", nickname)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + refreshExpiration))
            .signWith(key)
            .compact()
    }

    fun getNickname(token: String): String {
        return getClaims(token)["nickname"] as String
    }

    fun getEmail(token: String): String {
        return getClaims(token).subject
    }

    fun getUserId(token: String): Long {
        // JWT Claims의 숫자는 Integer로 파싱되므로 Number로 받아서 toLong() 변환
        return (getClaims(token)["userId"] as Number).toLong()
    }

    fun isValid(token: String): Boolean {
        return runCatching { getClaims(token) }.isSuccess
    }

    /** 토큰 남은 만료시간(ms) 반환 → 블랙리스트 TTL에 사용 */
    fun getRemainingExpiration(token: String): Long {
        val expiration = getClaims(token).expiration
        return (expiration.time - System.currentTimeMillis()).coerceAtLeast(0)
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}