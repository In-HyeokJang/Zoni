package com.zoni.feed.domain

import jakarta.persistence.*
import java.time.LocalDateTime

/**
 * 피드(게시글) 엔티티 — ZONI 청년 라이프 통합 플랫폼
 *
 * userId / nickname은 service-user를 직접 호출하지 않고 JWT에서 꺼낸 값을 비정규화해서 저장.
 * → 마이크로서비스 간 의존성 최소화
 *
 * placeId: service-place와 연동 (nullable → 장소 없이도 피드 작성 가능)
 * imageUrl: 대표 이미지 URL (nullable → 텍스트만 작성 가능)
 *
 * 카테고리:
 * - REVIEW    : 핫플 후기 (장소 방문 후기, 사진 공유)
 * - COURSE    : 당일 코스 공유 (데이트/여행 코스)
 * - PHOTO     : 사진 공유 (일상, 풍경 등)
 * - COMMUNITY : 자유 소통 (청년 라이프 전반)
 */
@Entity
@Table(
    name = "feeds",
    indexes = [
        Index(name = "idx_feeds_user_id",   columnList = "user_id"),
        Index(name = "idx_feeds_category",  columnList = "category"),
        Index(name = "idx_feeds_place_id",  columnList = "place_id"),
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

    /**
     * service-place의 장소 ID (nullable)
     * Phase 2에서 Place Service 구현 후 연동
     */
    @Column(name = "place_id")
    var placeId: Long? = null,

    /**
     * 대표 이미지 URL (nullable)
     * Phase 2에서 이미지 업로드 구현 후 사용
     */
    @Column(name = "image_url", length = 500)
    var imageUrl: String? = null,

    @Column(nullable = false)
    var viewCount: Int = 0,

    @Column(nullable = false)
    var likeCount: Int = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),

    /** 소프트 삭제 */
    @Column(nullable = false)
    var isDeleted: Boolean = false,

    /** 동시성 방어 (낙관적 락) */
    @Version
    var version: Long = 0
)

enum class FeedCategory {
    REVIEW,     // 핫플 후기 (장소 방문 후기, 사진 공유)
    COURSE,     // 당일 코스 공유 (데이트/여행 코스)
    PHOTO,      // 사진 공유 (일상, 풍경)
    COMMUNITY   // 자유 소통 (청년 라이프 전반)
}
