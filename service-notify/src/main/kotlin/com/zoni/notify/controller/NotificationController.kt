package com.zoni.notify.controller

import com.zoni.common.ApiResponse
import com.zoni.common.JwtPrincipal
import com.zoni.notify.service.NotificationService
import com.zoni.notify.service.SseEmitterManager
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val sseEmitterManager: SseEmitterManager
) {

    /**
     * SSE 실시간 알림 스트림 연결.
     * 클라이언트는 이 endpoint에 연결을 유지하고, 알림 발생 시 즉시 수신.
     */
    @GetMapping("/stream", produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun stream(@AuthenticationPrincipal principal: JwtPrincipal): SseEmitter {
        return sseEmitterManager.connect(principal.userId)
    }

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