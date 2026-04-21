package com.zoni.notify.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.notify.domain.Notification
import com.zoni.notify.domain.NotificationType
import com.zoni.notify.dto.response.NotificationPageResponse
import com.zoni.notify.dto.response.NotificationResponse
import com.zoni.notify.event.FeedCreatedEvent
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
        val pageable = PageRequest.of(0, Int.MAX_VALUE)
        notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .content
            .filter { !it.isRead }
            .forEach { it.isRead = true }
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