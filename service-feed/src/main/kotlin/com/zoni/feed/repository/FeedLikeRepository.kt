package com.zoni.feed.repository

import com.zoni.feed.domain.FeedLike
import org.springframework.data.jpa.repository.JpaRepository

interface FeedLikeRepository : JpaRepository<FeedLike, Long> {
    fun findByUserIdAndFeedId(userId: Long, feedId: Long): FeedLike?
    fun existsByUserIdAndFeedId(userId: Long, feedId: Long): Boolean
}