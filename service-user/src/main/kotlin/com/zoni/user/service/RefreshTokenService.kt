package com.zoni.user.service

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * Redis 기반 Refresh Token 관리 서비스
 *
 * 저장 구조:
 *   key   → "refresh:{userId}"
 *   value → "{refreshToken 문자열}"
 *   TTL   → 7일 (access token 만료 후 재발급에 사용)
 *
 * 로그아웃 시 → delete() 로 토큰 즉시 무효화
 * 재발급 시   → isValid() 로 Redis 저장값과 비교 후 신규 발급
 */
@Service
class RefreshTokenService(
    private val redisTemplate: StringRedisTemplate
) {
    companion object {
        private const val PREFIX = "refresh:"
        private const val TTL_DAYS = 7L
    }

    /** Refresh Token 저장 (로그인, 재발급 시 호출) */
    fun save(userId: Long, refreshToken: String) {
        redisTemplate.opsForValue().set(
            key(userId),
            refreshToken,
            TTL_DAYS,
            TimeUnit.DAYS
        )
    }

    /** Redis에 저장된 Refresh Token 조회 */
    fun get(userId: Long): String? =
        redisTemplate.opsForValue().get(key(userId))

    /** Refresh Token 삭제 (로그아웃 시 호출) */
    fun delete(userId: Long) {
        redisTemplate.delete(key(userId))
    }

    /**
     * 요청으로 받은 refreshToken 이 Redis에 저장된 값과 동일한지 검증
     * → 탈취된 토큰으로 재발급 시도를 방어
     */
    fun isValid(userId: Long, refreshToken: String): Boolean =
        get(userId) == refreshToken

    private fun key(userId: Long) = "$PREFIX$userId"
}

