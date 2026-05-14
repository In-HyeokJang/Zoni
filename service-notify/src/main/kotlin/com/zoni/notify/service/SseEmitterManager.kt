package com.zoni.notify.service

import com.zoni.notify.dto.response.NotificationResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.util.concurrent.ConcurrentHashMap

/**
 * [SSE Emitter 관리]
 *
 * userId별 SseEmitter를 ConcurrentHashMap으로 관리.
 * 알림 발생 시 해당 userId가 연결 중이면 SSE로 즉시 push.
 */
@Component
class SseEmitterManager {

    private val emitters = ConcurrentHashMap<Long, SseEmitter>()

    companion object {
        private const val TIMEOUT_MS = 30 * 60 * 1000L  // 30분
    }

    /**
     * SSE 연결 수립.
     * 연결 직후 "connect" 이벤트를 전송해 브라우저가 스트림을 인식하도록 함.
     */
    fun connect(userId: Long): SseEmitter {
        val emitter = SseEmitter(TIMEOUT_MS)

        emitter.onCompletion { emitters.remove(userId) }
        emitter.onTimeout   { emitters.remove(userId); emitter.complete() }
        emitter.onError     { emitters.remove(userId) }

        emitters[userId] = emitter

        try {
            emitter.send(SseEmitter.event().name("connect").data("connected"))
        } catch (e: Exception) {
            emitters.remove(userId)
        }

        return emitter
    }

    /**
     * 특정 userId에게 알림 이벤트 전송.
     * 연결되지 않은 유저는 무시 (polling으로 나중에 수신).
     */
    fun send(userId: Long, notification: NotificationResponse) {
        val emitter = emitters[userId] ?: return
        try {
            emitter.send(SseEmitter.event().name("notification").data(notification))
        } catch (e: Exception) {
            emitters.remove(userId)
        }
    }
}