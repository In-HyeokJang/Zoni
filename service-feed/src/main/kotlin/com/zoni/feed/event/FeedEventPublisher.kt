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
        const val TOPIC_FEED_CREATED   = "feed.created"
        const val TOPIC_FEED_LIKED     = "feed.liked"
        const val TOPIC_FEED_COMMENTED = "feed.commented"
    }

    fun publishFeedCreated(event: FeedCreatedEvent) {
        kafkaTemplate.send(TOPIC_FEED_CREATED, event.feedId.toString(), event)
        log.info("[Kafka] 피드 생성 이벤트 발행 - feedId: {}, userId: {}", event.feedId, event.userId)
    }

    fun publishFeedLiked(event: FeedLikedEvent) {
        kafkaTemplate.send(TOPIC_FEED_LIKED, event.feedId.toString(), event)
        log.info("[Kafka] 피드 좋아요 이벤트 발행 - feedId: {}, likerUserId: {}", event.feedId, event.likerUserId)
    }

    fun publishFeedCommented(event: FeedCommentedEvent) {
        kafkaTemplate.send(TOPIC_FEED_COMMENTED, event.feedId.toString(), event)
        log.info("[Kafka] 피드 댓글 이벤트 발행 - feedId: {}, commenterId: {}", event.feedId, event.commenterId)
    }
}