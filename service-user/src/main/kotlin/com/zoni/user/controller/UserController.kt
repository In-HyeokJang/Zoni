package com.zoni.user.controller

import com.zoni.common.ApiResponse
import com.zoni.user.dto.request.BirthYearUpdateRequest
import com.zoni.user.dto.request.KakaoLoginRequest
import com.zoni.user.dto.request.LoginRequest
import com.zoni.user.dto.request.SignUpRequest
import com.zoni.user.dto.request.TokenRefreshRequest
import com.zoni.user.dto.response.LoginResponse
import com.zoni.user.dto.response.UserResponse
import com.zoni.user.service.KakaoOAuthService
import com.zoni.user.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal

/**
 * 사용자 서비스 컨트롤러 (service-user:8081)
 *
 * 회원가입·로그인·토큰 관리·내 정보 조회 등 인증 관련 API를 제공한다.
 *
 * 공개(비인증) API:
 *   - POST /api/users/signup         회원가입
 *   - POST /api/users/login          이메일 로그인
 *   - POST /api/users/login/kakao    카카오 로그인 (프론트 연동)
 *   - GET  /api/users/login/kakao/callback  카카오 콜백 (로컬 테스트용)
 *   - POST /api/users/refresh        토큰 재발급
 *
 * 인증 필요 API:
 *   - POST  /api/users/logout              로그아웃
 *   - GET   /api/users/me                  내 정보 조회
 *   - PATCH /api/users/me/birth-year       출생 연도 업데이트 (Phase 3-B)
 */
@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val kakaoOAuthService: KakaoOAuthService
) {

    /**
     * 회원가입
     *
     * 생성된 userId를 반환한다. 이메일 중복 시 409 에러.
     * birthYear 는 선택 사항이며, 청년 혜택 나이 필터에 사용된다.
     *
     * POST /api/users/signup
     */
    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    fun signUp(@Valid @RequestBody request: SignUpRequest): ApiResponse<Long> =
        ApiResponse.ok(userService.signUp(request))

    /**
     * 이메일 로그인
     *
     * accessToken + refreshToken 반환. 이미 로그인 상태여도 신규 토큰 발급(덮어쓰기).
     *
     * POST /api/users/login
     */
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(userService.login(request))

    /**
     * Access Token 재발급
     *
     * 인증 불필요 (refresh token 자체가 인증 수단).
     * Refresh Token이 만료되거나 Redis에 없으면 401 반환.
     *
     * POST /api/users/refresh
     */
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: TokenRefreshRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(userService.refresh(request))

    /**
     * 로그아웃
     *
     * Redis에서 refresh token 삭제 + access token 블랙리스트 등록.
     *
     * POST /api/users/logout
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
     *
     * JWT 인증 필요. birthYear 포함 (Phase 3-B).
     *
     * GET /api/users/me
     */
    @GetMapping("/me")
    fun getMe(principal: Principal): ApiResponse<UserResponse> =
        ApiResponse.ok(userService.getMe(principal.name))

    /**
     * 출생 연도 업데이트 (Phase 3-B: 청년 혜택 연령 필터용)
     *
     * 회원가입 시 미입력 또는 이후 변경이 필요할 때 호출.
     * 미래 연도 등록 불가 → 400 에러.
     *
     * PATCH /api/users/me/birth-year
     */
    @PatchMapping("/me/birth-year")
    fun updateBirthYear(
        principal: Principal,
        @Valid @RequestBody request: BirthYearUpdateRequest
    ): ApiResponse<Unit> {
        userService.updateBirthYear(principal.name, request)
        return ApiResponse.ok(Unit)
    }

    /**
     * 카카오 로그인 (프론트 연동용)
     *
     * 프론트에서 카카오 SDK로 받은 "인가 코드"를 전달 → JWT 반환.
     *
     * POST /api/users/login/kakao
     */
    @PostMapping("/login/kakao")
    fun kakaoLogin(@Valid @RequestBody request: KakaoLoginRequest): ApiResponse<LoginResponse> =
        ApiResponse.ok(kakaoOAuthService.kakaoLogin(request.code))

    /**
     * 카카오 OAuth 콜백 (로컬 테스트용)
     *
     * 카카오가 리다이렉트하는 URL. 브라우저에서 바로 JWT 확인 가능.
     * redirect_uri를 카카오 콘솔에 등록 필요:
     *   http://localhost:8081/api/users/login/kakao/callback
     *
     * GET /api/users/login/kakao/callback?code=XXX
     */
    @GetMapping("/login/kakao/callback")
    fun kakaoCallback(@RequestParam code: String): ApiResponse<LoginResponse> =
        ApiResponse.ok(kakaoOAuthService.kakaoLogin(code))
}
