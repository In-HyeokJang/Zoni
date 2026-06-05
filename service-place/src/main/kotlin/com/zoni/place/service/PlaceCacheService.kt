package com.zoni.place.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.type.TypeFactory
import com.zoni.common.BenefitType
import com.zoni.place.dto.response.PlaceResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

/**
 * 장소 관련 Redis 캐싱 서비스
 *
 * 캐시 키 전략:
 * - 검색 결과: place:search:{keyword}:{x}:{y}:{page}:{size}       TTL 1시간
 * - 인기 장소: place:popular                                       TTL 30분
 * - 혜택 장소: place:benefits:{benefitType}:{page}:{size}         TTL 1시간  [Phase 3-B]
 * - 연령별 혜택: place:benefits:age:{age}:{page}:{size}           TTL 1시간  [Phase 3-B]
 */
@Service
class PlaceCacheService(
    private val redisTemplate: StringRedisTemplate,
    private val objectMapper: ObjectMapper,

    @Value("\${cache.search-ttl:3600}")
    private val searchTtl: Long,

    @Value("\${cache.popular-ttl:1800}")
    private val popularTtl: Long,

    /** 혜택 장소 캐시 TTL (초 단위, 기본 1시간) */
    @Value("\${cache.benefit-ttl:3600}")
    private val benefitTtl: Long
) {
    private val log = LoggerFactory.getLogger(javaClass)

    // ── 검색 캐시 ─────────────────────────────────────────────────────────

    fun <T> getSearchCache(key: String, type: Class<T>): T? =
        runCatching {
            redisTemplate.opsForValue().get(key)?.let { objectMapper.readValue(it, type) }
        }.getOrElse {
            log.warn("[PlaceCache] 캐시 읽기 실패 - key: {}", key)
            null
        }

    fun setSearchCache(key: String, value: Any) {
        runCatching {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), searchTtl, TimeUnit.SECONDS)
        }.onFailure { log.warn("[PlaceCache] 캐시 저장 실패 - key: {}", key) }
    }

    fun searchCacheKey(keyword: String, x: Double?, y: Double?, page: Int, size: Int): String =
        "place:search:${keyword}:${x ?: ""}:${y ?: ""}:$page:$size"

    // ── 인기 장소 캐시 ────────────────────────────────────────────────────

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

    fun setPopularCache(key: String, value: Any) {
        runCatching {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), popularTtl, TimeUnit.SECONDS)
        }.onFailure { log.warn("[PlaceCache] 캐시 저장 실패 - key: {}", key) }
    }

    fun evictPopularCache() {
        redisTemplate.delete("place:popular")
    }

    // ── Phase 3-B: 혜택 장소 캐시 ────────────────────────────────────────

    /**
     * 혜택 장소 목록 캐시 조회
     * @param key benefitCacheKey() 또는 ageBenefitCacheKey() 로 생성한 키
     */
    fun getBenefitCache(key: String): List<PlaceResponse>? {
        val json = redisTemplate.opsForValue().get(key) ?: return null
        return runCatching {
            val listType = TypeFactory.defaultInstance()
                .constructCollectionType(List::class.java, PlaceResponse::class.java)
            objectMapper.readValue<List<PlaceResponse>>(json, listType)
        }.getOrElse {
            log.warn("[PlaceCache] 혜택 캐시 읽기 실패 - key: {}", key)
            null
        }
    }

    fun setBenefitCache(key: String, value: Any) {
        runCatching {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), benefitTtl, TimeUnit.SECONDS)
        }.onFailure { log.warn("[PlaceCache] 혜택 캐시 저장 실패 - key: {}", key) }
    }

    /** 특정 혜택 유형 캐시 무효화 */
    fun evictBenefitCache(benefitType: BenefitType? = null) {
        val pattern = if (benefitType != null) "place:benefits:${benefitType.name}*"
                      else "place:benefits:*"
        runCatching {
            val keys = redisTemplate.keys(pattern)
            if (keys.isNotEmpty()) redisTemplate.delete(keys)
        }.onFailure { log.warn("[PlaceCache] 혜택 캐시 무효화 실패 - pattern: {}", pattern) }
    }

    fun benefitCacheKey(benefitType: BenefitType?, page: Int, size: Int): String =
        "place:benefits:${benefitType?.name ?: "ALL"}:$page:$size"

    fun ageBenefitCacheKey(age: Int, page: Int, size: Int): String =
        "place:benefits:age:$age:$page:$size"
}
