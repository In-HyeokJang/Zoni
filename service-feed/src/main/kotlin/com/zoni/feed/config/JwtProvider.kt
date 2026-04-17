package com.zoni.feed.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

/**
 * service-feed 전용 JWT Provider
 *
 * - 토큰 발급은 service-user에서 담당
 * - service-feed는 토큰 검증 + 클레임 추출만 수행
 * - secret은 service-user와 동일 값 사용 → 동일 토큰 검증 가능
 */
@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String
) {
    private val key by lazy {
        Keys.hmacShaKeyFor(secret.toByteArray())
    }

    fun getEmail(token: String): String =
        getClaims(token).subject

    fun getUserId(token: String): Long =
        (getClaims(token)["userId"] as Number).toLong()

    fun getNickname(token: String): String =
        getClaims(token)["nickname"] as String

    fun isValid(token: String): Boolean =
        runCatching { getClaims(token) }.isSuccess

    private fun getClaims(token: String): Claims =
        Jwts.parser()
            .verifyWith(key)
            .build()
            .parseSignedClaims(token)
            .payload
}

