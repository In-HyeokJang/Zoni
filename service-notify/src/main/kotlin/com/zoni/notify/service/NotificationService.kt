package com.zoni.notify.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.notify.domain.Notification
import com.zoni.notify.domain.NotificationType
import com.zoni.notify.dto.response.NotificationPageResponse
import com.zoni.notify.dto.response.NotificationResponse
import com.zoni.notify.event.FeedCreatedEvent
import com.zoni.notify.event.FeedLikedEvent
import com.zoni.notify.repository.NotificationRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class NotificationService(
    private val notificationRepository: NotificationRepository
) {

    /** Kafka 이벤트 → 알림 생성 */
    @Transactional
    fun createFeedNotification(event: FeedCreatedEvent) {
        notificationRepository.save(
            Notification(
                userId      = event.userId,
                type        = NotificationType.FEED_CREATED,
                message     = "${event.nickname}님의 피드가 등록되었습니다: ${event.title}",
                referenceId = event.feedId
            )
        )
    }

    /** Kafka 이벤트 → 좋아요 알림 생성 (피드 작성자에게) */
    @Transactional
    fun createLikeNotification(event: FeedLikedEvent) {
        if (event.feedOwnerId == event.likerUserId) return  // 본인 좋아요는 알림 생략
        notificationRepository.save(
            Notification(
                userId      = event.feedOwnerId,
                type        = NotificationType.FEED_LIKED,
                message     = "${event.likerNickname}님이 회원님의 피드를 좋아합니다.",
                referenceId = event.feedId
            )
        )
    }

    /**
     * [Kafka 이벤트 → 피드 댓글 알림 생성 서비스]
     * 
     * 1. 이벤트를 수신받아 알림(Notification) 엔티티 생성
     * 2. 피드 작성자(feedOwnerId)에게 알림 발송
     */
    @Transactional
    fun createCommentNotification(event: com.zoni.notify.event.FeedCommentedEvent) {
        if (event.feedOwnerId == event.commenterId) return // 본인 댓글은 알림 생략
        notificationRepository.save(
            Notification(
                userId      = event.feedOwnerId,
                type        = NotificationType.FEED_COMMENTED,
                message     = "${event.commenterNickname}님이 댓글을 남겼습니다: ${event.content.take(20)}...",
                referenceId = event.feedId
            )
        )
    }

    /** 내 알림 목록 조회 */
    fun getMyNotifications(userId: Long, page: Int, size: Int): NotificationPageResponse {
        val pageable = PageRequest.of(page, size)
        val result = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
        val unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId)

        return NotificationPageResponse(
            notifications  = result.content.map { it.toResponse() },
            unreadCount    = unreadCount,
            totalElements  = result.totalElements,
            totalPages     = result.totalPages,
            currentPage    = page,
            isLast         = result.isLast
        )
    }

    /** 알림 읽음 처리 */
    @Transactional
    fun markAsRead(userId: Long, notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { ZoniException(ErrorCode.NOTIFICATION_NOT_FOUND) }

        if (notification.userId != userId) throw ZoniException(ErrorCode.FORBIDDEN)
        notification.isRead = true
    }

    /** 전체 읽음 처리 */
    @Transactional
    fun markAllAsRead(userId: Long) {
        notificationRepository.markAllAsReadByUserId(userId)
    }

    private fun Notification.toResponse() = NotificationResponse(
        id          = id,
        type        = type,
        message     = message,
        referenceId = referenceId,
        isRead      = isRead,
        createdAt   = createdAt
    )
}