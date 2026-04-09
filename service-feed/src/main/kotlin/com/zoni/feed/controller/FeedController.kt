package com.zoni.feed.controller

import com.zoni.common.ApiResponse
import com.zoni.common.JwtPrincipal
import com.zoni.feed.dto.request.FeedCreateRequest
import com.zoni.feed.dto.request.FeedUpdateRequest
import com.zoni.feed.dto.response.FeedPageResponse
import com.zoni.feed.dto.response.FeedResponse
import com.zoni.feed.service.FeedService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feeds")
class FeedController(
    private val feedService: FeedService
) {

    /**
     * 피드 목록 조회 (비로그인 가능)
     * GET /api/feeds?page=0&size=10&category=ROOM_WANTED
     */
    @GetMapping
    fun getFeeds(
        @RequestParam(defaultValue = "0")  page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @RequestParam(required = false)    category: String?
    ): ApiResponse<FeedPageResponse> =
        ApiResponse.ok(feedService.getFeeds(page, size, category))

    /**
     * 피드 상세 조회 (비로그인 가능 / 조회수 +1)
     * GET /api/feeds/{id}
     */
    @GetMapping("/{id}")
    fun getFeed(@PathVariable id: Long): ApiResponse<FeedResponse> =
        ApiResponse.ok(feedService.getFeed(id))

    /**
     * 피드 작성 (JWT 인증 필요)
     * POST /api/feeds
     * Authorization: Bearer {accessToken}
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createFeed(
        @Valid @RequestBody request: FeedCreateRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ApiResponse<FeedResponse> =
        ApiResponse.ok(feedService.createFeed(principal.userId, principal.nickname, request))

    /**
     * 피드 수정 (JWT 인증 + 본인만)
     * PUT /api/feeds/{id}
     * Authorization: Bearer {accessToken}
     */
    @PutMapping("/{id}")
    fun updateFeed(
        @PathVariable id: Long,
        @Valid @RequestBody request: FeedUpdateRequest,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ApiResponse<FeedResponse> =
        ApiResponse.ok(feedService.updateFeed(principal.userId, id, request))

    /**
     * 피드 삭제 (JWT 인증 + 본인만 / 소프트 삭제)
     * DELETE /api/feeds/{id}
     * Authorization: Bearer {accessToken}
     */
    @DeleteMapping("/{id}")
    fun deleteFeed(
        @PathVariable id: Long,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ApiResponse<Unit> {
        feedService.deleteFeed(principal.userId, id)
        return ApiResponse.ok(Unit)
    }

    /**
     * 내 피드 목록 (JWT 인증 필요)
     * GET /api/feeds/my?page=0&size=10
     * Authorization: Bearer {accessToken}
     */
    @GetMapping("/my")
    fun getMyFeeds(
        @RequestParam(defaultValue = "0")  page: Int,
        @RequestParam(defaultValue = "10") size: Int,
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ApiResponse<FeedPageResponse> =
        ApiResponse.ok(feedService.getMyFeeds(principal.userId, page, size))
}

