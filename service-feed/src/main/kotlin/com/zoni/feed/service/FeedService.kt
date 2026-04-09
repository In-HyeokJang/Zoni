package com.zoni.feed.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.feed.domain.Feed
import com.zoni.feed.domain.FeedCategory
import com.zoni.feed.dto.request.FeedCreateRequest
import com.zoni.feed.dto.request.FeedUpdateRequest
import com.zoni.feed.dto.response.FeedPageResponse
import com.zoni.feed.dto.response.FeedResponse
import com.zoni.feed.dto.response.FeedSummaryResponse
import com.zoni.feed.repository.FeedRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class FeedService(
    private val feedRepository: FeedRepository
) {

    /** 피드 목록 조회 (비로그인 가능 / 카테고리 필터 선택) */
    fun getFeeds(page: Int, size: Int, category: String?): FeedPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())

        val feedPage = if (category != null) {
            val feedCategory = runCatching { FeedCategory.valueOf(category) }
                .getOrElse { throw ZoniException(ErrorCode.BAD_REQUEST) }
            feedRepository.findByCategoryAndIsDeletedFalse(feedCategory, pageable)
        } else {
            feedRepository.findByIsDeletedFalse(pageable)
        }

        return FeedPageResponse(
            feeds         = feedPage.content.map { it.toSummaryResponse() },
            totalElements = feedPage.totalElements,
            totalPages    = feedPage.totalPages,
            currentPage   = page,
            pageSize      = size,
            isLast        = feedPage.isLast
        )
    }

    /** 피드 상세 조회 (조회수 +1) */
    @Transactional
    fun getFeed(id: Long): FeedResponse {
        val feed = findActiveFeed(id)
        feed.viewCount++
        return feed.toResponse()
    }

    /** 피드 작성 (JWT 인증 필요) */
    @Transactional
    fun createFeed(userId: Long, nickname: String, request: FeedCreateRequest): FeedResponse {
        val feed = Feed(
            userId   = userId,
            nickname = nickname,
            title    = request.title,
            content  = request.content,
            category = request.category
        )
        return feedRepository.save(feed).toResponse()
    }

    /** 피드 수정 (JWT 인증 + 본인만 가능) */
    @Transactional
    fun updateFeed(userId: Long, feedId: Long, request: FeedUpdateRequest): FeedResponse {
        val feed = findActiveFeed(feedId)

        if (feed.userId != userId) {
            throw ZoniException(ErrorCode.FORBIDDEN)
        }

        request.title?.let   { feed.title   = it }
        request.content?.let { feed.content = it }
        feed.updatedAt = LocalDateTime.now()

        return feed.toResponse()
    }

    /** 피드 삭제 - 소프트 삭제 (JWT 인증 + 본인만 가능) */
    @Transactional
    fun deleteFeed(userId: Long, feedId: Long) {
        val feed = findActiveFeed(feedId)

        if (feed.userId != userId) {
            throw ZoniException(ErrorCode.FORBIDDEN)
        }

        feed.isDeleted = true
        feed.updatedAt = LocalDateTime.now()
    }

    /** 내 피드 목록 (JWT 인증 필요) */
    fun getMyFeeds(userId: Long, page: Int, size: Int): FeedPageResponse {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val feedPage = feedRepository.findByUserIdAndIsDeletedFalse(userId, pageable)

        return FeedPageResponse(
            feeds         = feedPage.content.map { it.toSummaryResponse() },
            totalElements = feedPage.totalElements,
            totalPages    = feedPage.totalPages,
            currentPage   = page,
            pageSize      = size,
            isLast        = feedPage.isLast
        )
    }

    // ── 내부 헬퍼 ─────────────────────────────────────────────────

    private fun findActiveFeed(id: Long): Feed =
        feedRepository.findById(id)
            .filter { !it.isDeleted }
            .orElseThrow { ZoniException(ErrorCode.NOT_FOUND) }

    private fun Feed.toResponse() = FeedResponse(
        id         = id,
        userId     = userId,
        nickname   = nickname,
        title      = title,
        content    = content,
        category   = category.name,
        viewCount  = viewCount,
        likeCount  = likeCount,
        createdAt  = createdAt,
        updatedAt  = updatedAt
    )

    private fun Feed.toSummaryResponse() = FeedSummaryResponse(
        id        = id,
        userId    = userId,
        nickname  = nickname,
        title     = title,
        category  = category.name,
        viewCount = viewCount,
        likeCount = likeCount,
        createdAt = createdAt
    )
}

