package com.zoni.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer
import org.springframework.web.client.RestTemplate

/**
 * 인프라 Bean 설정 (Redis, RestTemplate 등)
 */
@Configuration
class InfraConfig {

    // ── Redis ────────────────────────────────────────────────────
    /**
     * StringRedisTemplate: key/value 모두 String으로 직렬화
     * RefreshToken, 블랙리스트 저장에 사용
     *   "refresh:{userId}"   → refreshToken 문자열
     *   "blacklist:{token}"  → "logout"
     */
    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
        val template = StringRedisTemplate()
        template.connectionFactory = connectionFactory
        template.keySerializer     = StringRedisSerializer()
        template.valueSerializer   = StringRedisSerializer()
        return template
    }

    // ── RestTemplate ─────────────────────────────────────────────
    /**
     * 카카오 OAuth API 호출에 사용
     * KakaoOAuthService 에서 주입받아 사용
     */
    @Bean
    fun restTemplate(): RestTemplate = RestTemplate()
}
