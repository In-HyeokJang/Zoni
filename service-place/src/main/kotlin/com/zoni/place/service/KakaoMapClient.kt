package com.zoni.place.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.place.dto.response.KakaoPlaceSearchResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder

/**
 * 카카오 로컬 API 클라이언트
 * 키워드로 주변 장소 검색 (https://dapi.kakao.com/v2/local/search/keyword.json)
 */
@Component
class KakaoMapClient(
    private val restTemplate: RestTemplate,

    @Value("\${kakao.rest-api-key}")
    private val restApiKey: String
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun searchPlaces(
        keyword: String,
        x: Double?,
        y: Double?,
        radius: Int = 5000,
        page: Int = 1,
        size: Int = 15
    ): KakaoPlaceSearchResponse {
        val uri = UriComponentsBuilder
            .fromHttpUrl("https://dapi.kakao.com/v2/local/search/keyword.json")
            .queryParam("query", keyword)
            .apply {
                if (x != null && y != null) {
                    queryParam("x", x)
                    queryParam("y", y)
                    queryParam("radius", radius)
                    queryParam("sort", "distance")
                } else {
                    queryParam("sort", "accuracy")
                }
            }
            .queryParam("page", page)
            .queryParam("size", size)
            .build()
            .encode()
            .toUri()

        val headers = HttpHeaders().apply {
            set("Authorization", "KakaoAK $restApiKey")
        }

        return runCatching {
            restTemplate.exchange(uri, HttpMethod.GET, HttpEntity<Void>(headers), KakaoPlaceSearchResponse::class.java).body
                ?: KakaoPlaceSearchResponse()
        }.getOrElse {
            log.error("[KakaoMapClient] 장소 검색 실패 - keyword: {}, error: {}", keyword, it.message)
            throw ZoniException(ErrorCode.KAKAO_MAP_API_ERROR)
        }
    }
}