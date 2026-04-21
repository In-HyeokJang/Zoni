package com.zoni.notify.dto.response

import com.zoni.notify.domain.NotificationType
import java.time.LocalDateTime

data class NotificationResponse(
    val id: Long,
    val type: NotificationType,
    val message: String,
    val referenceId: Long?,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

data class NotificationPageResponse(
    val notifications: List<NotificationResponse>,
    val unreadCount: Long,
    val totalElements: Long,
    val totalPages: Int,
    val currentPage: Int,
    val isLast: Boolean
)