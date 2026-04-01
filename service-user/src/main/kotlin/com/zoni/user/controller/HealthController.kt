package com.zoni.user.controller

import com.zoni.common.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class HealthController {

    @GetMapping("/health")
    fun health() = ApiResponse.ok(
        mapOf("status" to "UP", "service" to "user")
    )
}