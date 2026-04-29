package com.zoni.notify.event

data class FeedLikedEvent(
    val feedId: Long,
    val feedOwnerId: Long,
    val likerUserId: Long,
    val likerNickname: String
)