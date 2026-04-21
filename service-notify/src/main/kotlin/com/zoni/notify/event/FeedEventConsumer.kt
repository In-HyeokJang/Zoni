package com.zoni.notify.event

import com.zoni.notify.service.NotificationService
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

/**
 * Kafka Consumer — service-feed의 피드 생성 이벤트 소비
 *
 * 흐름:
 * service-feed가 피드 등록 → "feed.created" 토픽에 발행
 * → 이 Consumer가 소비 → 알림 DB 저장
 */
@Component
class FeedEventConsumer(
    private val notificationService: NotificationService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @KafkaListener(topics = ["feed.created"], groupId = "zoni-notify-group")
    fun handleFeedCreated(event: FeedCreatedEvent) {
        log.info("[Kafka] 피드 생성 이벤트 수신 - feedId: {}, userId: {}", event.feedId, event.userId)
        notificationService.createFeedNotification(event)
    }
}