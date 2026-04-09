package com.zoni.user.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * Redis 기반 Token 관리 서비스
 *
 * [RefreshToken 저장 구조]
 *   key   → "refresh:{userId}"
 *   value → "{refreshToken 문자열}"
 *   TTL   → 7일
 *
 * [AccessToken 블랙리스트 구조]
 *   key   → "blacklist:{accessToken}"
 *   value → "logout"
 *   TTL   → accessToken 남은 만료시간
 *   → 로그아웃한 토큰으로 재요청 시 차단
 */
@Service
class RefreshTokenService(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val REFRESH_PREFIX   = "refresh:"
        private const val BLACKLIST_PREFIX = "blacklist:"
        private const val TTL_DAYS = 7L
    }

    /** Refresh Token 저장 (로그인, 재발급 시 호출) */
    fun save(userId: Long, refreshToken: String) {
        redisTemplate.opsForValue().set(
            refreshKey(userId),
            refreshToken,
            TTL_DAYS,
            TimeUnit.DAYS
        )
    }

    /** Redis에 저장된 Refresh Token 조회 */
    fun get(userId: Long): String? =
        redisTemplate.opsForValue().get(refreshKey(userId))

    /** Refresh Token 삭제 (로그아웃 시 호출) */
    fun delete(userId: Long) {
        redisTemplate.delete(refreshKey(userId))
    }

    /** 요청으로 받은 refreshToken 이 Redis에 저장된 값과 동일한지 검증 */
    fun isValid(userId: Long, refreshToken: String): Boolean =
        get(userId) == refreshToken

    // ── AccessToken 블랙리스트 ─────────────────────────────

    /**
     * 로그아웃한 AccessToken 블랙리스트 등록
     * TTL = 토큰 남은 만료시간 → 만료되면 자동 삭제
     */
    fun addToBlacklist(accessToken: String, remainingMs: Long) {
        if (remainingMs > 0) {
            redisTemplate.opsForValue().set(
                blacklistKey(accessToken),
                "logout",
                remainingMs,
                TimeUnit.MILLISECONDS
            )
        }
    }

    /** AccessToken이 블랙리스트에 등록되어 있는지 확인 */
    fun isBlacklisted(accessToken: String): Boolean =
        redisTemplate.hasKey(blacklistKey(accessToken)) == true

    private fun refreshKey(userId: Long)       = "$REFRESH_PREFIX$userId"
    private fun blacklistKey(accessToken: String) = "$BLACKLIST_PREFIX$accessToken"
}

