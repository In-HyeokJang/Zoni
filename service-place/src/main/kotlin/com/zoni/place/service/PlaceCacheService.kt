package com.zoni.place.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.zoni.place.dto.response.PlaceResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * 장소 관련 Redis 캐싱 서비스
 *
 * - 검색 결과: place:search:{keyword}:{x}:{y}:{page}:{size}  TTL 1시간
 * - 인기 장소: place:popular                                  TTL 30분
 */
@Service
class PlaceCacheService(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,

    @Value("\${cache.search-ttl:3600}")
    private val searchTtl: Long,

    @Value("\${cache.popular-ttl:1800}")
    private val popularTtl: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun <T> getSearchCache(key: String, type: Class<T>): T? =
        runCatching {
            redisTemplate.opsForValue().get(key)?.let { objectMapper.readValue(it, type) }
        }.getOrElse {
            log.warn("[PlaceCache] 캐시 읽기 실패 - key: {}", key)
            null
        }

    fun getPopularCache(key: String): List<PlaceResponse>? {
        val json = redisTemplate.opsForValue().get(key) ?: return null
        return runCatching {
            val listType = TypeFactory.defaultInstance()
                .constructCollectionType(List::class.java, PlaceResponse::class.java)
            objectMapper.readValue<List<PlaceResponse>>(json, listType)
        }.getOrElse {
            log.warn("[PlaceCache] 캐시 읽기 실패 - key: {}", key)
            null
        }
    }

    fun setSearchCache(key: String, value: Any) {
        runCatching {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), searchTtl, TimeUnit.SECONDS)
        }.onFailure { log.warn("[PlaceCache] 캐시 저장 실패 - key: {}", key) }
    }

    fun setPopularCache(key: String, value: Any) {
        runCatching {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), popularTtl, TimeUnit.SECONDS)
        }.onFailure { log.warn("[PlaceCache] 캐시 저장 실패 - key: {}", key) }
    }

    fun evictPopularCache() {
        redisTemplate.delete("place:popular")
    }

    fun searchCacheKey(keyword: String, x: Double?, y: Double?, page: Int, size: Int): String =
        "place:search:${keyword}:${x ?: ""}:${y ?: ""}:$page:$size"
}