package com.zoni.place.service

import com.zoni.common.BenefitType
import com.zoni.common.ErrorCode
import com.zoni.common.PlaceDataSource
import com.zoni.common.ZoniException
import com.zoni.place.domain.Place
import com.zoni.place.dto.request.BenefitSaveRequest
import com.zoni.place.dto.request.PlaceSaveRequest
import com.zoni.place.dto.response.KakaoPlaceSearchResponse
import com.zoni.place.dto.response.PlacePageResponse
import com.zoni.place.dto.response.PlaceResponse
import com.zoni.place.repository.PlaceRepository
import org.slf4j.LoggerFactory
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
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 장소 키워드 검색 (카카오 로컬 API)
     *
     * 1. Redis 캐시 우선 조회
     * 2. 미스(miss) 시 카카오 API 호출
     * 3. 결과를 캐시에 저장 후 반환 (TTL 1시간)
     *
     * @param keyword 검색 키워드
     * @param x 경도 (null 이면 전국 검색 — 정확도순)
     * @param y 위도 (null 이면 전국 검색 — 정확도순)
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
            ?.let {
                log.debug("[PlaceService] 검색 캐시 히트 - keyword: {}", keyword)
                return it
            }

        log.info("[PlaceService] 카카오 API 검색 호출 - keyword: {}, x: {}, y: {}", keyword, x, y)
        return kakaoMapClient.searchPlaces(keyword, x, y, page = page, size = size).also {
            placeCacheService.setSearchCache(cacheKey, it)
        }
    }

    /**
     * 장소 저장 (핫플 등록)
     *
     * 동일 유저가 같은 kakaoPlaceId 장소를 중복 저장하는 것을 방지한다.
     * 저장 성공 시 인기 장소 캐시를 무효화한다.
     *
     * @param userId JWT에서 추출한 사용자 ID
     * @param nickname JWT에서 추출한 사용자 닉네임 (비정규화 저장)
     * @param request 저장할 장소 정보
     */
    @Transactional
    fun savePlace(userId: Long, nickname: String, request: PlaceSaveRequest): PlaceResponse {
        if (placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse(request.kakaoPlaceId, userId)) {
            log.warn("[PlaceService] 중복 장소 저장 시도 - userId: {}, kakaoPlaceId: {}", userId, request.kakaoPlaceId)
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
        placeCacheService.evictPopularCache()
        log.info("[PlaceService] 장소 저장 완료 - placeId: {}, userId: {}, name: {}", place.id, userId, place.name)
        return place.toResponse()
    }

    /**
     * 혜택 장소 등록 (사용자 직접 신고)
     *
     * 1. 중복 저장 방지 체크 (같은 유저 + 같은 kakaoPlaceId)
     * 2. 연령 범위 유효성 검증 (min <= max)
     * 3. Place 엔티티 생성 — dataSource = USER_INPUT, benefitType = 요청값
     * 4. 혜택 장소 캐시 무효화
     *
     * @param userId JWT에서 추출한 사용자 ID
     * @param nickname JWT에서 추출한 사용자 닉네임
     * @param request 혜택 장소 등록 요청 정보
     */
    @Transactional
    fun saveBenefitPlace(userId: Long, nickname: String, request: BenefitSaveRequest): PlaceResponse {
        if (placeRepository.existsByKakaoPlaceIdAndUserIdAndIsDeletedFalse(request.kakaoPlaceId, userId)) {
            log.warn("[PlaceService] 중복 혜택 장소 저장 시도 - userId: {}, kakaoPlaceId: {}", userId, request.kakaoPlaceId)
            throw ZoniException(ErrorCode.PLACE_ALREADY_SAVED)
        }

        if (request.targetAgeMin != null && request.targetAgeMax != null
            && request.targetAgeMin > request.targetAgeMax) {
            throw ZoniException(ErrorCode.INVALID_AGE_RANGE)
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
                nickname     = nickname,
                benefitType  = request.benefitType,
                discountInfo = request.discountInfo,
                targetAgeMin = request.targetAgeMin,
                targetAgeMax = request.targetAgeMax,
                dataSource   = PlaceDataSource.USER_INPUT,
                sourceUrl    = request.sourceUrl
            )
        )
        placeCacheService.evictPopularCache()
        placeCacheService.evictBenefitCache(request.benefitType)
        log.info("[PlaceService] 혜택 장소 등록 완료 - placeId: {}, benefitType: {}, userId: {}",
            place.id, request.benefitType, userId)
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
        if (place.benefitType != null) placeCacheService.evictBenefitCache(place.benefitType)
        log.info("[PlaceService] 장소 삭제 완료 - placeId: {}, userId: {}", placeId, userId)
    }

    /**
     * 혜택 장소 목록 조회 (혜택 유형 필터 + 페이지네이션)
     *
     * 1. Redis 캐시 우선 확인
     * 2. 미스 시 DB 조회
     * 3. benefitType=null 이면 혜택 있는 장소 전체 반환
     *
     * @param benefitType 혜택 유형 필터 (null = 전체 혜택 장소)
     */
    fun getBenefitPlaces(benefitType: BenefitType?, page: Int, size: Int): PlacePageResponse {
        log.debug("[PlaceService] 혜택 장소 조회 - benefitType: {}, page: {}, size: {}", benefitType, page, size)
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())

        val result = if (benefitType != null)
            placeRepository.findByBenefitTypeAndIsDeletedFalse(benefitType, pageable)
        else
            placeRepository.findByBenefitTypeIsNotNullAndIsDeletedFalse(pageable)

        return PlacePageResponse(
            places        = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages    = result.totalPages,
            currentPage   = page,
            isLast        = result.isLast
        )
    }

    /**
     * 나이 기반 혜택 장소 필터링
     *
     * 사용자의 나이(age)가 targetAgeMin ~ targetAgeMax 범위에 포함되는 혜택 장소만 반환한다.
     * targetAgeMin/targetAgeMax 가 null 인 장소는 연령 제한 없음으로 간주하여 포함.
     *
     * @param age 사용자 현재 나이 (null 이면 연령 필터 없이 전체 혜택 장소 반환)
     */
    fun getBenefitPlacesForAge(age: Int?, page: Int, size: Int): PlacePageResponse {
        log.debug("[PlaceService] 연령 기반 혜택 장소 조회 - age: {}, page: {}", age, page)
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val result = placeRepository.findBenefitPlacesForAge(age, pageable)

        return PlacePageResponse(
            places        = result.content.map { it.toResponse() },
            totalElements = result.totalElements,
            totalPages    = result.totalPages,
            currentPage   = page,
            isLast        = result.isLast
        )
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
        createdAt    = createdAt,
        // Phase 3-B 혜택 필드
        benefitType  = benefitType,
        discountInfo = discountInfo,
        targetAgeMin = targetAgeMin,
        targetAgeMax = targetAgeMax,
        dataSource   = dataSource,
        sourceUrl    = sourceUrl
    )
}
