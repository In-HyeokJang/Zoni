package com.zoni.notify.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "notifications",
    indexes = [Index(name = "idx_notifications_user_id", columnList = "user_id")]
)
class Notification(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    /** 알림 받는 사용자 */
    @Column(name = "user_id", nullable = false)
    val userId: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: NotificationType,

    /** 알림 메시지 */
    @Column(nullable = false)
    val message: String,

    /** 연관 리소스 ID (feedId 등) */
    @Column(name = "reference_id")
    val referenceId: Long? = null,

    /** 읽음 여부 */
    var isRead: Boolean = false,

    val createdAt: LocalDateTime = LocalDateTime.now()
)

enum class NotificationType {
    FEED_CREATED   // 피드 등록 알림
}