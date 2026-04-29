package com.zoni.feed.repository

import com.zoni.feed.domain.Comment
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository : JpaRepository<Comment, Long> {
    /**
     * 특정 피드에 달린 댓글 목록을 조회합니다.
     * 오래된 댓글부터 보여주기 위해 정렬은 호출부에서 제어(Sort.by("createdAt").ascending())합니다.
     */
    fun findByFeedIdAndIsDeletedFalse(feedId: Long, pageable: Pageable): Page<Comment>
}
