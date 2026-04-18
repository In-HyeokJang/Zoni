package com.zoni.place.controller

import com.zoni.common.ApiResponse
import com.zoni.common.JwtPrincipal
import com.zoni.place.dto.request.PlaceSaveRequest
import com.zoni.place.dto.response.KakaoPlaceSearchResponse
import com.zoni.place.dto.response.PlacePageResponse
import com.zoni.place.dto.response.PlaceResponse
import com.zoni.place.service.PlaceService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/places")
class PlaceController(
    private val placeService: PlaceService
) {

    /**
     * 장소 키워드 검색 (비로그인 가능)
     * GET /api/places/search?keyword=홍대&x=126.9&y=37.5&page=1&size=15
     * x, y 없으면 전국 검색 (정확도순), 있으면 주변 검색 (거리순)
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
     * 인기 장소 목록 (비로그인 가능, Redis 캐시)
     * GET /api/places/popular?size=10
     */
    @GetMapping("/popular")
    fun getPopularPlaces(
        @RequestParam(defaultValue = "10") size: Int
    ): ApiResponse<List<PlaceResponse>> =
        ApiResponse.ok(placeService.getPopularPlaces(size))

    /**
     * 저장된 장소 상세 조회 (비로그인 가능)
     * GET /api/places/{id}
     */
    @GetMapping("/{id}")
    fun getPlace(@PathVariable id: Long): ApiResponse<PlaceResponse> =
        ApiResponse.ok(placeService.getPlace(id))

    /**
     * 장소 저장 (JWT 필요)
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
     * DELETE /api/places/{id}
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deletePlace(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable id: Long
    ) = placeService.deletePlace(principal.userId, id)
}