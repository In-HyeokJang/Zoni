package com.zoni.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

/**
 * Redis 설정
 *
 * StringRedisTemplate: key/value 모두 String으로 직렬화
 * RefreshToken 저장에 사용 → "refresh:{userId}" : "{refreshToken}"
 *
 * application-local.yml 에서 host/port 읽어옴:
 *   spring.data.redis.host=localhost
 *   spring.data.redis.port=6379
 */
@Configuration
class RedisConfig {

    @Bean
    fun stringRedisTemplate(connectionFactory: RedisConnectionFactory): StringRedisTemplate {
        val template = StringRedisTemplate()
        template.connectionFactory = connectionFactory
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        return template
    }
}

