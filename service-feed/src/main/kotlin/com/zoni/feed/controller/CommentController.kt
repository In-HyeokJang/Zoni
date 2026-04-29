package com.zoni.feed.controller

import com.zoni.common.ApiResponse
import com.zoni.common.JwtPrincipal
import com.zoni.feed.dto.request.CommentCreateRequest
import com.zoni.feed.dto.response.CommentResponse
import com.zoni.feed.service.CommentService
import jakarta.validation.Valid
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/feeds")
class CommentController(
    private val commentService: CommentService
) {

    /** 댓글 작성 */
    @PostMapping("/{feedId}/comments")
    fun addComment(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable feedId: Long,
        @RequestBody @Valid request: CommentCreateRequest
    ): ApiResponse<CommentResponse> {
        val result = commentService.addComment(principal.userId, principal.nickname, feedId, request)
        return ApiResponse.ok(result)
    }

    /** 댓글 목록 조회 (비로그인도 가능, 본인 작성 여부 판단을 위해 Optional Principal 사용) */
    @GetMapping("/{feedId}/comments")
    fun getComments(
        @AuthenticationPrincipal principal: JwtPrincipal?,
        @PathVariable feedId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ApiResponse<List<CommentResponse>> {
        val requesterId = principal?.userId ?: -1L
        val result = commentService.getComments(requesterId, feedId, page, size)
        return ApiResponse.ok(result)
    }

    /** 댓글 삭제 (댓글 작성자 또는 피드 작성자) */
    @DeleteMapping("/comments/{commentId}")
    fun deleteComment(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable commentId: Long
    ): ApiResponse<Unit> {
        commentService.deleteComment(principal.userId, commentId)
        return ApiResponse.ok(Unit)
    }
}
