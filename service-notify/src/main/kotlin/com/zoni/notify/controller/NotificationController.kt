package com.zoni.notify.controller

import com.zoni.common.ApiResponse
import com.zoni.common.JwtPrincipal
import com.zoni.notify.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {

    @GetMapping
    fun getMyNotifications(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int
    ): ResponseEntity<ApiResponse<*>> {
        val result = notificationService.getMyNotifications(principal.userId, page, size)
        return ResponseEntity.ok(ApiResponse.ok(result))
    }

    @PatchMapping("/{notificationId}/read")
    fun markAsRead(
        @AuthenticationPrincipal principal: JwtPrincipal,
        @PathVariable notificationId: Long
    ): ResponseEntity<ApiResponse<*>> {
        notificationService.markAsRead(principal.userId, notificationId)
        return ResponseEntity.ok(ApiResponse.ok(null))
    }

    @PatchMapping("/read-all")
    fun markAllAsRead(
        @AuthenticationPrincipal principal: JwtPrincipal
    ): ResponseEntity<ApiResponse<*>> {
        notificationService.markAllAsRead(principal.userId)
        return ResponseEntity.ok(ApiResponse.ok(null))
    }
}