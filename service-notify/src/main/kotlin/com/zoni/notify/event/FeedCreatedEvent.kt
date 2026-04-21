package com.zoni.notify.event

/** service-feed의 FeedCreatedEvent와 동일한 구조 */
data class FeedCreatedEvent(
    val feedId: Long = 0,
    val userId: Long = 0,
    val nickname: String = "",
    val title: String = "",
    val category: String = ""
)