package com.zoni.feed.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 피드 댓글(Comment) 엔티티
 * 
 * Feed 엔티티와 강하게 결합(@ManyToOne)하지 않고, feedId만 저장하는 방식을 채택합니다.
 * 이를 통해 향후 Feed 서비스와 Comment 서비스가 물리적으로 분리되더라도 문제가 없도록 MSA 지향적 설계를 유지합니다.
 * 
 * userId, nickname 역시 JWT의 Claim 값을 비정규화하여 저장합니다.
 */
@Entity
@Table(
    name = "comments",
    indexes = [
        Index(name = "idx_comments_feed_id", columnList = "feed_id"),
        Index(name = "idx_comments_user_id", columnList = "user_id")
    ]
)
class Comment(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "feed_id", nullable = false)
    val feedId: Long,

    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Column(nullable = false, length = 50)
    val nickname: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "is_deleted", nullable = false)
    var isDeleted: Boolean = false
)
