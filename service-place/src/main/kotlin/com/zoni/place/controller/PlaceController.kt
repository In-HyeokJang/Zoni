package com.zoni.place.controller

import com.zoni.common.ApiResponse
import com.zoni.common.BenefitType
import com.zoni.common.JwtPrincipal
import com.zoni.place.dto.request.BenefitSaveRequest
import com.zoni.place.dto.request.PlaceSaveRequest
import com.zoni.place.dto.response.KakaoPlaceSearchResponse
import com.zoni.place.dto.response.PlacePageResponse
import com.zoni.place.dto.response.PlaceResponse
import com.zoni.place.service.PlaceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 장소 서비스 컨트롤러 (service-place:8084)
 *
 * 카카오맵 장소 검색·저장 기능과 Phase 3-B 혜택 레이어 API를 제공한다.
 *
 * 공개(비인증) API:
 *   - GET  /api/places/search      키워드 검색
 *   - GET  /api/places/popular     인기 장소
 *   - GET  /api/places/{id}        상세 조회
 *   - GET  /api/places/benefits    혜택 장소 목록 (Phase 3-B)
 *
 * 인증 필요 API:
 *   - POST   /api/places            장소 저장
 *   - GET    /api/places/my         내 저장 장소
 *   - DELETE /api/places/{id}       장소 삭제
 *   - POST   /api/places/benefit    혜택 장소 등록 (Phase 3-B)
 */
@RestController
@RequestMapping("/api/places")
class PlaceController(
    private val placeService: PlaceService
) {

    /**
     * 장소 키워드 검색 (비로그인 가능)
     *
     * x, y 좌표 없으면 전국 검색(정확도순), 있으면 주변 검색(거리순).
     * 결과는 Redis에 1시간 캐싱된다.
     *
     * GET /api/places/search?keyword=홍대&x=126.9&y=37.5&page=1&size=15
     */
    @GetMapping("/search")
    fun searchPlaces(
        @RequestParam keyword: String,
        @RequestParam(required = false) x: Double?,
        @RequestParam(required = false) y: Double?,
        @RequestParam(defaultValue = "1") page: Int,
        @RequestParam(defaultValue = "15") size: Int
    ): ApiResponse<KakaoPlaceSearchResponse> =
        ApiResponse.ok(placeService.searchPlaces(keyword, x, y, page, size))

    /**
     * 인기 장소 목록 (비로그인 가능)
     *
     * 저장 횟수가 많은 장소 순으로 반환한다. Redis 30분 캐싱.
     *
     * GET /api/places/popular?size=10
     */
    @GetMapping("/popular")
    fun getPopularPlaces(
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<List<PlaceResponse>> =
        ApiResponse.ok(placeService.getPopularPlaces(size))

    /**
     * 저장된 장소 상세 조회 (비로그인 가능)
     *
     * 조회할 때마다 viewCount 가 1 증가한다.
     *
     * GET /api/places/{id}
     */
    @GetMapping("/{id}")
    fun getPlace(@PathVariable id: Long): ApiResponse<PlaceResponse> =
        ApiResponse.ok(placeService.getPlace(id))

    /**
     * 장소 저장 (JWT 필요)
     *
     * 같은 유저가 동일 kakaoPlaceId를 중복 저장하면 409 에러.
     *
     * POST /api/places
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun savePlace(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @Valid @RequestBody request: PlaceSaveRequest
    ): ApiResponse<PlaceResponse> =
        ApiResponse.ok(placeService.savePlace(principal.userId, principal.nickname, request))

    /**
     * 내가 저장한 장소 목록 (JWT 필요)
     *
     * 최신 저장 순으로 반환한다. 페이지네이션 지원.
     *
     * GET /api/places/my?page=0&size=10
     */
    @GetMapping("/my")
    fun getMyPlaces(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<PlacePageResponse> =
        ApiResponse.ok(placeService.getMyPlaces(principal.userId, page, size))

    /**
     * 저장한 장소 삭제 (JWT + 본인)
     *
     * 소프트 삭제(isDeleted = true). 다른 사람의 장소는 403 에러.
     *
     * DELETE /api/places/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePlace(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable id: Long
    ) = placeService.deletePlace(principal.userId, id)

    // ── Phase 3-B: 혜택 레이어 API ────────────────────────────────────────

    /**
     * 혜택 장소 목록 조회 (비로그인 가능) [Phase 3-B]
     *
     * 혜택 유형 필터 또는 나이 기반 필터를 지원한다.
     *   - benefitType 지정 시: 해당 유형 혜택 장소만 반환
     *   - age 지정 시: 해당 나이가 대상인 혜택 장소만 반환 (targetAgeMin~Max 범위)
     *   - 둘 다 없으면: 혜택 있는 장소 전체 반환
     *   - age 와 benefitType 동시 지정 시: age 필터 우선 적용
     *
     * GET /api/places/benefits?benefitType=YOUTH_CULTURE_PASS&page=0&size=10
     * GET /api/places/benefits?age=25&page=0&size=10
     *
     * @param benefitType BenefitType enum 문자열 (YOUTH_CULTURE_PASS / LOCAL_YOUTH_DISCOUNT 등)
     * @param age 현재 나이 (이 나이가 혜택 대상 연령 범위에 포함되는 장소만 반환)
     */
    @GetMapping("/benefits")
    fun getBenefitPlaces(
        @RequestParam(required = false) benefitType: BenefitType?,
        @RequestParam(required = false) age: Int?,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<PlacePageResponse> {
        val result = if (age != null)
            placeService.getBenefitPlacesForAge(age, page, size)
        else
            placeService.getBenefitPlaces(benefitType, page, size)
        return ApiResponse.ok(result)
    }

    /**
     * 혜택 장소 직접 등록 (JWT 필요) [Phase 3-B]
     *
     * 사용자가 "이 장소는 청년 혜택이 있어요"라고 직접 신고·등록한다.
     * benefitType 과 discountInfo 는 필수. dataSource = USER_INPUT 으로 자동 저장.
     * 같은 유저가 동일 kakaoPlaceId를 중복 등록하면 409 에러.
     *
     * POST /api/places/benefit
     */
    @PostMapping("/benefit")
    @ResponseStatus(HttpStatus.CREATED)
    fun saveBenefitPlace(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @Valid @RequestBody request: BenefitSaveRequest
    ): ApiResponse<PlaceResponse> =
        ApiResponse.ok(placeService.saveBenefitPlace(principal.userId, principal.nickname, request))
}
