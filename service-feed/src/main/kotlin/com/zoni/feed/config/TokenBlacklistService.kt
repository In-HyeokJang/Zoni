package com.zoni.feed.config

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

/**
 * service-user에서 로그아웃한 access token 블랙리스트 확인
 *
 * service-user의 RefreshTokenService.addToBlacklist()가 "blacklist:{token}" 키로 등록하고,
 * 여기서 동일한 키로 존재 여부를 확인한다.
 * → 로그아웃한 토큰으로 service-feed에 요청해도 차단됨
 */
@Service
class TokenBlacklistService(
    private val redisTemplate: StringRedisTemplate
) {
    fun isBlacklisted(accessToken: String): Boolean =
        redisTemplate.hasKey("blacklist:$accessToken") == true
}
