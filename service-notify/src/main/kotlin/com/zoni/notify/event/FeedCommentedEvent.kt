package com.zoni.notify.event

data class FeedCommentedEvent(
    val feedId: Long,
    val feedOwnerId: Long,
    val commentId: Long,
    val commenterId: Long,
    val commenterNickname: String,
    val content: String
)
