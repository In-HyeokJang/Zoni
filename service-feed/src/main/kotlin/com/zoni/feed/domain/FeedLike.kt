package com.zoni.feed.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "feed_likes",
    uniqueConstraints = [UniqueConstraint(name = "uq_feed_likes_user_feed", columnNames = ["user_id", "feed_id"])],
    indexes = [Index(name = "idx_feed_likes_feed_id", columnList = "feed_id")]
)
class FeedLike(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(name = "feed_id", nullable = false)
    val feedId: Long,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)