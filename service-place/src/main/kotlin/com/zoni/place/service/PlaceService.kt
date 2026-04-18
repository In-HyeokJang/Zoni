package com.zoni.place.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.place.domain.Place
import com.zoni.place.dto.request.PlaceSaveRequest
import com.zoni.place.dto.response.KakaoPlaceSearchResponse
import com.zoni.place.dto.response.PlacePageResponse
import com.zoni.place.dto.response.PlaceResponse
import com.zoni.place.repository.PlaceRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class PlaceService(
    private val placeRepository: PlaceRepository,
    private val kakaoMapClient: KakaoMapClient,
    private val placeCacheService: PlaceCacheService
) {

    /**
     * 장소 키워드 검색 (카카오 로컬 API)
     * - Redis 캐시 우선 조회 → 없으면 카카오 API 호출 후 캐싱
     */
    fun searchPlaces(
        keyword: String,
        x: Double?,
        y: Double?,
        page: Int,
        size: Int
    ): KakaoPlaceSearchResponse {
        val cacheKey = placeCacheService.searchCacheKey(keyword, x, y, page, size)

        placeCacheService.getSearchCache(cacheKey, KakaoPlaceSearchResponse::class.java)
            ?.let { return it }

        return kakaoMapClient.searchPlaces(keyword, x, y, page = page, size = size).also {
            placeCacheService.setSearchCache(cacheKey, it)
        }
    }

    /**
     * 장소 저장 (핫플 등록)
     * - 동일 유저가 같은 장소를 중복 저장 불가
     */
    @Transactional
    fun savePlace(userId: Long, nickname: String, request: PlaceSaveRequest): PlaceResponse {
        if (placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse(request.kakaoPlaceId, userId)) {
            throw ZoniException(ErrorCode.PLACE_ALREADY_SAVED)
        }

        val place = placeRepository.save(
            Place(
                kakaoPlaceId = request.kakaoPlaceId,
                name         = request.name,
                category     = request.category,
                address      = request.address,
                roadAddress  = request.roadAddress,
                phone        = request.phone,
                placeUrl     = request.placeUrl,
                x            = request.x,
                y            = request.y,
                userId       = userId,
                nickname     = nickname
            )
        )

        // 저장 이벤트 발생 → 인기 장소 캐시 무효화
        placeCacheService.evictPopularCache()

        return place.toResponse()
    }

    /** 저장된 장소 상세 조회 (조회수 +1) */
    @Transactional
    fun getPlace(id: Long): PlaceResponse {
        val place = placeRepository.findByIdAndIsDeletedFalse(id)
            ?: throw ZoniException(ErrorCode.PLACE_NOT_FOUND)
        place.viewCount++
        return place.toResponse()
    }

    /** 인기 장소 목록 (저장 횟수 많은 순, Redis 캐싱) */
    fun getPopularPlaces(size: Int): List<PlaceResponse> {
        placeCacheService.getPopularCache("place:popular")
            ?.let { return it }

        return placeRepository.findPopularPlaces(size)
            .map { it.toResponse() }
            .also { placeCacheService.setPopularCache("place:popular", it) }
    }

    /** 내가 저장한 장소 목록 */
    fun getMyPlaces(userId: Long, page: Int, size: Int): PlacePageResponse {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = placeRepository.findByUserIdAndIsDeletedFalse(userId, pageable)

        return PlacePageResponse(
            places        = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages    = result.totalPages,
            currentPage   = page,
            isLast        = result.isLast
        )
    }

    /** 저장한 장소 삭제 (소프트 삭제, 본인만 가능) */
    @Transactional
    fun deletePlace(userId: Long, placeId: Long) {
        val place = placeRepository.findByIdAndIsDeletedFalse(placeId)
            ?: throw ZoniException(ErrorCode.PLACE_NOT_FOUND)

        if (place.userId != userId) throw ZoniException(ErrorCode.FORBIDDEN)

        place.isDeleted = true
        place.updatedAt = LocalDateTime.now()

        placeCacheService.evictPopularCache()
    }

    private fun Place.toResponse() = PlaceResponse(
        id           = id,
        kakaoPlaceId = kakaoPlaceId,
        name         = name,
        category     = category,
        address      = address,
        roadAddress  = roadAddress,
        phone        = phone,
        placeUrl     = placeUrl,
        x            = x,
        y            = y,
        userId       = userId,
        nickname     = nickname,
        viewCount    = viewCount,
        createdAt    = createdAt
    )
}