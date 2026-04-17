package com.zoni.feed.repository

import com.zoni.feed.domain.Feed
import com.zoni.feed.domain.FeedCategory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface FeedRepository : JpaRepository<Feed, Long> {

    /** 전체 피드 목록 (삭제 제외, 최신순) */
    fun findByIsDeletedFalse(pageable: Pageable): Page<Feed>

    /** 카테고리별 피드 목록 (삭제 제외) */
    fun findByCategoryAndIsDeletedFalse(category: FeedCategory, pageable: Pageable): Page<Feed>

    /** 내 피드 목록 (userId 기준, 삭제 제외) */
    fun findByUserIdAndIsDeletedFalse(userId: Long, pageable: Pageable): Page<Feed>
}

