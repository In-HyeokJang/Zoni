package com.zoni.feed.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.feed.domain.Feed
import com.zoni.feed.domain.FeedCategory
import com.zoni.feed.domain.FeedLike
import com.zoni.feed.event.FeedEventPublisher
import com.zoni.feed.repository.FeedLikeRepository
import com.zoni.feed.repository.FeedRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class FeedServiceTest {

    @Mock lateinit var feedRepository: FeedRepository
    @Mock lateinit var feedLikeRepository: FeedLikeRepository
    @Mock lateinit var feedEventPublisher: FeedEventPublisher

    lateinit var feedService: FeedService

    @BeforeEach
    fun setUp() {
        feedService = FeedService(feedRepository, feedLikeRepository, feedEventPublisher)
    }

    // ── 픽스처 ────────────────────────────────────────────────────────

    private fun feed(id: Long = 1L, ownerId: Long = 10L, likeCount: Int = 0) = Feed(
        id        = id,
        userId    = ownerId,
        nickname  = "작성자",
        title     = "테스트 피드",
        content   = "내용",
        category  = FeedCategory.COMMUNITY
    ).also { it.likeCount = likeCount }

    private fun feedLike(userId: Long = 99L, feedId: Long = 1L) =
        FeedLike(userId = userId, feedId = feedId)

    // ── toggleLike ────────────────────────────────────────────────────

    @Test
    fun `처음 좋아요 누르면 isLiked true, likeCount 1, Kafka 이벤트 발행`() {
        given(feedRepository.findById(1L)).willReturn(Optional.of(feed()))
        given(feedLikeRepository.findByUserIdAndFeedId(99L, 1L)).willReturn(null)
        whenever(feedLikeRepository.save(any())).thenAnswer { it.arguments[0] as FeedLike }

        val result = feedService.toggleLike(userId = 99L, nickname = "좋아요유저", feedId = 1L)

        assertThat(result.isLiked).isTrue()
        assertThat(result.likeCount).isEqualTo(1)
        verify(feedEventPublisher).publishFeedLiked(any())
    }

    @Test
    fun `이미 좋아요한 피드 다시 누르면 isLiked false, likeCount 감소, 이벤트 미발행`() {
        given(feedRepository.findById(1L)).willReturn(Optional.of(feed(likeCount = 1)))
        given(feedLikeRepository.findByUserIdAndFeedId(99L, 1L)).willReturn(feedLike())

        val result = feedService.toggleLike(userId = 99L, nickname = "좋아요유저", feedId = 1L)

        assertThat(result.isLiked).isFalse()
        assertThat(result.likeCount).isEqualTo(0)
        verify(feedEventPublisher, never()).publishFeedLiked(any())
        verify(feedLikeRepository).delete(any())
    }

    @Test
    fun `likeCount가 0인 상태에서 취소해도 음수가 되지 않는다`() {
        given(feedRepository.findById(1L)).willReturn(Optional.of(feed(likeCount = 0)))
        given(feedLikeRepository.findByUserIdAndFeedId(99L, 1L)).willReturn(feedLike())

        val result = feedService.toggleLike(userId = 99L, nickname = "좋아요유저", feedId = 1L)

        assertThat(result.likeCount).isEqualTo(0)  // maxOf(0, -1) → 0 보장
    }

    @Test
    fun `존재하지 않는 피드 좋아요 시도 → FEED_NOT_FOUND 예외`() {
        given(feedRepository.findById(999L)).willReturn(Optional.empty())

        val ex = assertThrows<ZoniException> {
            feedService.toggleLike(userId = 99L, nickname = "유저", feedId = 999L)
        }
        assertThat(ex.errorCode).isEqualTo(ErrorCode.FEED_NOT_FOUND)
    }

    @Test
    fun `삭제된 피드 좋아요 시도 → FEED_NOT_FOUND 예외`() {
        val deleted = feed().also { it.isDeleted = true }
        given(feedRepository.findById(1L)).willReturn(Optional.of(deleted))

        val ex = assertThrows<ZoniException> {
            feedService.toggleLike(userId = 99L, nickname = "유저", feedId = 1L)
        }
        assertThat(ex.errorCode).isEqualTo(ErrorCode.FEED_NOT_FOUND)
    }
}