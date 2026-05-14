package com.zoni.notify.service

import com.zoni.notify.domain.Notification
import com.zoni.notify.domain.NotificationType
import com.zoni.notify.event.FeedCommentedEvent
import com.zoni.notify.event.FeedCreatedEvent
import com.zoni.notify.event.FeedLikedEvent
import com.zoni.notify.repository.NotificationRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {

    @Mock lateinit var notificationRepository: NotificationRepository
    @Mock lateinit var sseEmitterManager: SseEmitterManager

    lateinit var notificationService: NotificationService

    @BeforeEach
    fun setUp() {
        notificationService = NotificationService(notificationRepository, sseEmitterManager)
    }

    // ─── createFeedNotification ────────────────────────────────────────────

    @Test
    fun `피드 생성 알림 - 저장 후 SSE push 호출`() {
        val event = FeedCreatedEvent(userId = 1L, feedId = 10L, nickname = "jay", title = "서울 핫플")
        val saved = notification(userId = 1L, type = NotificationType.FEED_CREATED)
        given(notificationRepository.save(any<Notification>())).willReturn(saved)

        notificationService.createFeedNotification(event)

        verify(notificationRepository).save(any())
        verify(sseEmitterManager).send(any(), any())
    }

    // ─── createLikeNotification ────────────────────────────────────────────

    @Test
    fun `좋아요 알림 - 피드 작성자에게 저장 및 SSE push`() {
        val event = FeedLikedEvent(feedOwnerId = 1L, likerUserId = 2L, likerNickname = "bob", feedId = 10L)
        val saved = notification(userId = 1L, type = NotificationType.FEED_LIKED)
        given(notificationRepository.save(any<Notification>())).willReturn(saved)

        notificationService.createLikeNotification(event)

        verify(notificationRepository).save(any())
        verify(sseEmitterManager).send(any(), any())
    }

    @Test
    fun `좋아요 알림 - 본인 좋아요는 저장 및 SSE push 없음`() {
        val event = FeedLikedEvent(feedOwnerId = 1L, likerUserId = 1L, likerNickname = "jay", feedId = 10L)

        notificationService.createLikeNotification(event)

        verify(notificationRepository, never()).save(any())
        verify(sseEmitterManager, never()).send(any(), any())
    }

    // ─── createCommentNotification ─────────────────────────────────────────

    @Test
    fun `댓글 알림 - 피드 작성자에게 저장 및 SSE push`() {
        val event = FeedCommentedEvent(feedId = 10L, feedOwnerId = 1L, commentId = 100L, commenterId = 2L, commenterNickname = "bob", content = "좋아요!")
        val saved = notification(userId = 1L, type = NotificationType.FEED_COMMENTED)
        given(notificationRepository.save(any<Notification>())).willReturn(saved)

        notificationService.createCommentNotification(event)

        verify(notificationRepository).save(any())
        verify(sseEmitterManager).send(any(), any())
    }

    @Test
    fun `댓글 알림 - 본인 댓글은 저장 및 SSE push 없음`() {
        val event = FeedCommentedEvent(feedId = 10L, feedOwnerId = 1L, commentId = 100L, commenterId = 1L, commenterNickname = "jay", content = "내 댓글")

        notificationService.createCommentNotification(event)

        verify(notificationRepository, never()).save(any())
        verify(sseEmitterManager, never()).send(any(), any())
    }

    // ─── helper ───────────────────────────────────────────────────────────

    private fun notification(userId: Long, type: NotificationType) = Notification(
        id          = 1L,
        userId      = userId,
        type        = type,
        message     = "테스트 알림",
        referenceId = 10L,
        createdAt   = LocalDateTime.now()
    )
}