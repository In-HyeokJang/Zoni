package com.zoni.user.controller

import com.zoni.common.ApiResponse
import com.zoni.user.dto.request.LoginRequest
import com.zoni.user.dto.request.SignUpRequest
import com.zoni.user.dto.request.TokenRefreshRequest
import com.zoni.user.dto.response.LoginResponse
import com.zoni.user.dto.response.UserResponse
import com.zoni.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService
) {
    /** 회원가입 → 생성된 userId 반환 */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<Long> =
        ApiResponse.ok(userService.signUp(request))

    /** 로그인 → accessToken + refreshToken 반환 */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(userService.login(request))

    /**
     * Access Token 재발급
     * - 인증 불필요 (refresh token 자체가 인증 수단)
     * - refresh token이 만료되거나 Redis에 없으면 401 반환
     */
    @PostMapping("/refresh")
    fun refresh(@RequestBody request: TokenRefreshRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(userService.refresh(request))

    /**
     * 로그아웃
     * - JWT 인증 필요 (Authorization: Bearer {accessToken})
     * - Redis에서 refresh token 삭제 + access token 블랙리스트 등록
     */
    @PostMapping("/logout")
    fun logout(
        principal: Principal,
        @RequestHeader("Authorization") authorization: String
    ): ApiResponse<Unit> {
        val accessToken = authorization.removePrefix("Bearer ").trim()
        userService.logout(principal.name, accessToken)
        return ApiResponse.ok(Unit)
    }

    /**
     * 내 정보 조회
     * - JWT 인증 필요 (Authorization: Bearer {accessToken})
     */
    @GetMapping("/me")
    fun getMe(principal: Principal): ApiResponse<UserResponse> =
        ApiResponse.ok(userService.getMe(principal.name))
}