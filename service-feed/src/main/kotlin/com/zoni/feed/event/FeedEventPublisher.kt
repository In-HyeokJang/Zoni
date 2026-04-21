package com.zoni.feed.event

import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class FeedEventPublisher(
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(javaClass)

    companion object {
        const val TOPIC_FEED_CREATED = "feed.created"
    }

    fun publishFeedCreated(event: FeedCreatedEvent) {
        kafkaTemplate.send(TOPIC_FEED_CREATED, event.feedId.toString(), event)
        log.info("[Kafka] 피드 생성 이벤트 발행 - feedId: {}, userId: {}", event.feedId, event.userId)
    }
}