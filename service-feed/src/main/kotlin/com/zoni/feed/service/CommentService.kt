package com.zoni.feed.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.feed.domain.Comment
import com.zoni.feed.dto.request.CommentCreateRequest
import com.zoni.feed.dto.response.CommentResponse
import com.zoni.feed.event.FeedCommentedEvent
import com.zoni.feed.event.FeedEventPublisher
import com.zoni.feed.repository.CommentRepository
import com.zoni.feed.repository.FeedRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class CommentService(
    private val commentRepository: CommentRepository,
    private val feedRepository: FeedRepository,
    private val feedEventPublisher: FeedEventPublisher
) {

    /**
     * [피드 댓글 추가 서비스]
     *
     * 1. 피드 존재 여부 확인 (Active 상태인지 확인)
     * 2. Comment 엔티티 생성 및 DB 저장
     * 3. 피드 작성자에게 알림 전송을 위한 Kafka 이벤트(`feed.commented`) 발행
     *    - 단, 본인이 자신의 피드에 댓글을 단 경우 알림 발행을 생략합니다.
     *
     * @param userId 댓글 작성자의 ID
     * @param nickname 댓글 작성자의 닉네임
     * @param feedId 댓글을 달 피드의 ID
     * @param request 댓글 등록 요청 정보 (내용)
     * @return 생성된 댓글 정보 응답 객체
     */
    @Transactional
    fun addComment(userId: Long, nickname: String, feedId: Long, request: CommentCreateRequest): CommentResponse {
        val feed = feedRepository.findById(feedId)
            .filter { !it.isDeleted }
            .orElseThrow { ZoniException(ErrorCode.FEED_NOT_FOUND) }

        val comment = commentRepository.save(
            Comment(
                feedId = feedId,
                userId = userId,
                nickname = nickname,
                content = request.content
            )
        )

        // 피드 작성자가 다른 사람일 경우에만 알림 이벤트 발행
        if (feed.userId != userId) {
            feedEventPublisher.publishFeedCommented(
                FeedCommentedEvent(
                    feedId = feed.id,
                    feedOwnerId = feed.userId,
                    commentId = comment.id,
                    commenterId = userId,
                    commenterNickname = nickname,
                    content = request.content
                )
            )
        }

        return comment.toResponse(userId)
    }

    /**
     * [피드 댓글 목록 조회 서비스]
     *
     * 1. 특정 피드(feedId)에 달린 댓글 중 삭제되지 않은 것만 조회
     * 2. 오래된 댓글이 위에, 최근 댓글이 아래에 오도록 `createdAt` 기준 오름차순(ascending) 정렬 (인스타그램, 쓰레드 방식)
     * 3. DTO로 변환하여 반환
     *
     * @param requesterId 요청한 유저 ID (isMine 판단을 위함, 비로그인 시 0 같은 임의의 값 전달)
     * @param feedId 피드 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     */
    fun getComments(requesterId: Long, feedId: Long, page: Int, size: Int): List<CommentResponse> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").ascending())
        val commentPage = commentRepository.findByFeedIdAndIsDeletedFalse(feedId, pageable)
        return commentPage.content.map { it.toResponse(requesterId) }
    }

    /**
     * [피드 댓글 삭제 서비스]
     *
     * 1. 댓글 존재 여부 및 Active 상태 확인
     * 2. 피드 존재 여부 확인 (권한 체크를 위해)
     * 3. 권한 체크:
     *    - 댓글을 작성한 본인(userId)이 삭제 가능
     *    - 또는, 해당 댓글이 달린 피드의 작성자(feed.userId)가 삭제 가능
     * 4. 권한 불일치 시 FORBIDDEN 에러 발생
     * 5. 상태값을 변경하여 소프트 삭제 처리
     *
     * @param userId 삭제를 요청한 유저의 ID
     * @param commentId 삭제할 댓글의 ID
     */
    @Transactional
    fun deleteComment(userId: Long, commentId: Long) {
        val comment = commentRepository.findById(commentId)
            .filter { !it.isDeleted }
            .orElseThrow { ZoniException(ErrorCode.COMMENT_NOT_FOUND) }

        val feed = feedRepository.findById(comment.feedId)
            .filter { !it.isDeleted }
            .orElseThrow { ZoniException(ErrorCode.FEED_NOT_FOUND) }

        // 삭제 권한: 댓글 작성자 본인 OR 피드 작성자 본인
        val hasPermission = (comment.userId == userId) || (feed.userId == userId)

        if (!hasPermission) {
            throw ZoniException(ErrorCode.FORBIDDEN)
        }

        comment.isDeleted = true
        comment.updatedAt = LocalDateTime.now()
    }

    private fun Comment.toResponse(requesterId: Long) = CommentResponse(
        id = id,
        feedId = feedId,
        userId = userId,
        nickname = nickname,
        content = content,
        createdAt = createdAt,
        isMine = (this.userId == requesterId)
    )
}
