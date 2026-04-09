package com.zoni.feed.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 피드(게시글) 엔티티
 *
 * userId / nickname은 service-user를 직접 호출하지 않고 JWT에서 꺼낸 값을 비정규화해서 저장.
 * → 마이크로서비스 간 의존성 최소화
 *
 * 카테고리:
 * - ROOM_WANTED : 방 구해요
 * - ROOM_OFFER  : 방 나눠요
 * - COMMUNITY   : 동네 이야기
 * - QNA         : 질문/답변
 */
@Entity
@Table(
    name = "feeds",
    indexes = [
        Index(name = "idx_feeds_user_id", columnList = "user_id"),
        Index(name = "idx_feeds_category", columnList = "category"),
        Index(name = "idx_feeds_created_at", columnList = "created_at")
    ]
)
class Feed(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** JWT에서 추출한 userId (service-user의 users.id 와 동일) */
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    /** 비정규화: 피드 목록에 표시할 작성자 닉네임 (JWT claim에서 가져옴) */
    @Column(nullable = false, length = 50)
    val nickname: String,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "TEXT", nullable = false)
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    val category: FeedCategory,

    @Column(nullable = false)
    var viewCount: Int = 0,

    @Column(nullable = false)
    var likeCount: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    /** 소프트 삭제 → 실제 DB에서 제거하지 않고 isDeleted=true 로 처리 */
    @Column(nullable = false)
    var isDeleted: Boolean = false
)

enum class FeedCategory {
    ROOM_WANTED,  // 방 구해요
    ROOM_OFFER,   // 방 나눠요
    COMMUNITY,    // 동네 이야기
    QNA           // 질문/답변
}

