package com.zoni.feed.event

data class FeedCreatedEvent(
    val feedId: Long,
    val userId: Long,
    val nickname: String,
    val title: String,
    val category: String
)