package com.zoni.notify.config

import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class JwtProvider(
    @Value("\${jwt.secret}") private val secret: String
) {
    private val key by lazy { Keys.hmacShaKeyFor(secret.toByteArray()) }

    fun getEmail(token: String): String = getClaims(token).subject
    fun getUserId(token: String): Long = (getClaims(token)["userId"] as Number).toLong()
    fun getNickname(token: String): String = getClaims(token)["nickname"] as String
    fun isValid(token: String): Boolean = runCatching { getClaims(token) }.isSuccess

    private fun getClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}