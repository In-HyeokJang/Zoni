package com.zoni.place.config

import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service

@Service
class TokenBlacklistService(
    private val redisTemplate: StringRedisTemplate
) {
    fun isBlacklisted(token: String): Boolean =
        redisTemplate.hasKey("blacklist:$token") == true
}