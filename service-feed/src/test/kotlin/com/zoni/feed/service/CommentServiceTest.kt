package com.zoni.feed.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.feed.domain.Comment
import com.zoni.feed.domain.Feed
import com.zoni.feed.domain.FeedCategory
import com.zoni.feed.dto.request.CommentCreateRequest
import com.zoni.feed.event.FeedCommentedEvent
import com.zoni.feed.event.FeedEventPublisher
import com.zoni.feed.repository.CommentRepository
import com.zoni.feed.repository.FeedRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import java.util.*

@ExtendWith(MockitoExtension::class)
class CommentServiceTest {

    @Mock lateinit var commentRepository: CommentRepository
    @Mock lateinit var feedRepository: FeedRepository
    @Mock lateinit var feedEventPublisher: FeedEventPublisher

    lateinit var commentService: CommentService

    @BeforeEach
    fun setUp() {
        commentService = CommentService(commentRepository, feedRepository, feedEventPublisher)
    }

    @Test
    fun `피드 작성자가 아닌 사용자가 댓글을 달면 이벤트가 정상적으로 발행된다`() {
        // given
        val feedOwnerId = 1L
        val commenterId = 2L
        val feedId = 10L
        val request = CommentCreateRequest("좋은 코스네요!")
        
        val feed = Feed(
            id = feedId,
            userId = feedOwnerId,
            nickname = "작성자",
            title = "제목",
            content = "내용",
            category = FeedCategory.COURSE
        )
        
        val comment = Comment(
            id = 100L,
            feedId = feedId,
            userId = commenterId,
            nickname = "댓글러",
            content = request.content
        )

        whenever(feedRepository.findById(feedId)).thenReturn(Optional.of(feed))
        whenever(commentRepository.save(any())).thenReturn(comment)

        // when
        val response = commentService.addComment(commenterId, "댓글러", feedId, request)

        // then
        assertThat(response.id).isEqualTo(100L)
        assertThat(response.content).isEqualTo("좋은 코스네요!")
        
        // 피드 작성자 != 댓글 작성자 이므로 알림 이벤트 발행됨
        verify(feedEventPublisher).publishFeedCommented(any<FeedCommentedEvent>())
    }

    @Test
    fun `피드 작성자 본인이 댓글을 달면 알림 이벤트가 발행되지 않는다`() {
        // given
        val ownerId = 1L
        val feedId = 10L
        val request = CommentCreateRequest("내가 단 댓글")
        
        val feed = Feed(
            id = feedId,
            userId = ownerId,
            nickname = "작성자",
            title = "제목",
            content = "내용",
            category = FeedCategory.COURSE
        )
        
        val comment = Comment(
            id = 100L,
            feedId = feedId,
            userId = ownerId,
            nickname = "작성자",
            content = request.content
        )

        whenever(feedRepository.findById(feedId)).thenReturn(Optional.of(feed))
        whenever(commentRepository.save(any())).thenReturn(comment)

        // when
        commentService.addComment(ownerId, "작성자", feedId, request)

        // then
        // 알림 이벤트 미발행 검증
        verifyNoInteractions(feedEventPublisher)
    }

    @Test
    fun `댓글 삭제 권한 불일치 시 FORBIDDEN 예외 발생`() {
        // given
        val feedOwnerId = 1L
        val commenterId = 2L
        val otherUserId = 3L
        val commentId = 100L

        val feed = Feed(id = 10L, userId = feedOwnerId, nickname = "작성자", title = "", content = "", category = FeedCategory.COURSE)
        val comment = Comment(id = commentId, feedId = 10L, userId = commenterId, nickname = "댓글러", content = "")

        whenever(commentRepository.findById(commentId)).thenReturn(Optional.of(comment))
        whenever(feedRepository.findById(10L)).thenReturn(Optional.of(feed))

        // when & then
        val exception = assertThrows<ZoniException> {
            commentService.deleteComment(otherUserId, commentId)
        }
        assertThat(exception.errorCode).isEqualTo(ErrorCode.FORBIDDEN)
    }
}
