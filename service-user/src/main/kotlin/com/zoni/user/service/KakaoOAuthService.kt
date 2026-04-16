package com.zoni.user.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.user.config.JwtProvider
import com.zoni.user.domain.OAuthProvider
import com.zoni.user.domain.User
import com.zoni.user.dto.response.KakaoTokenResponse
import com.zoni.user.dto.response.KakaoUserInfoResponse
import com.zoni.user.dto.response.LoginResponse
import com.zoni.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate

/**
 * 카카오 OAuth 로그인 서비스
 *
 * 흐름:
 * 1. 프론트에서 카카오 인가 코드 전달
 * 2. 인가 코드 → 카카오 AccessToken 교환
 * 3. 카카오 AccessToken → 사용자 정보 조회
 * 4. 기존 회원이면 로그인, 신규면 자동 가입
 * 5. 우리 서비스 JWT 발급 후 반환
 */
@Service
class KakaoOAuthService(
    private val userRepository: UserRepository,
    private val jwtProvider: JwtProvider,
    private val refreshTokenService: RefreshTokenService,
    private val restTemplate: RestTemplate,

    @Value("\${kakao.rest-api-key}")
    private val restApiKey: String,

    @Value("\${kakao.redirect-uri}")
    private val redirectUri: String
) {

    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 카카오 로그인 전체 처리
     * @param code 카카오 인가 코드 (프론트에서 전달)
     * @return 우리 서비스 JWT (LoginResponse)
     */
    fun kakaoLogin(code: String): LoginResponse {
        // 1단계: 인가 코드 → 카카오 AccessToken
        val kakaoToken = getKakaoToken(code)
        log.info("[Kakao OAuth] 토큰 발급 성공")

        // 2단계: 카카오 AccessToken → 사용자 정보
        val userInfo = getKakaoUserInfo(kakaoToken.accessToken)
        log.info("[Kakao OAuth] 사용자 정보 조회 성공 - kakaoId: {}", userInfo.id)

        // 3단계: 회원 처리 (신규 가입 or 기존 로그인)
        val user = findOrCreateUser(userInfo)

        // 4단계: 우리 서비스 JWT 발급
        val accessToken  = jwtProvider.generateToken(user.id, user.email, user.nickname)
        val refreshToken = jwtProvider.generateRefreshToken(user.id, user.email, user.nickname)
        refreshTokenService.save(user.id, refreshToken)

        log.info("[Kakao OAuth] 로그인 완료 - userId: {}, email: {}", user.id, user.email)

        return LoginResponse(
            accessToken     = accessToken,
            refreshToken    = refreshToken,
            userId          = user.id,
            email           = user.email,
            nickname        = user.nickname,
            profileImageUrl = user.profileImageUrl,
            oauthProvider   = user.oauthProvider?.name
        )
    }

    // ── 카카오 API 호출 ──────────────────────────────────────────────────

    /**
     * [Step 1] 인가 코드 → 카카오 AccessToken 교환
     * POST https://kauth.kakao.com/oauth/token
     */
    private fun getKakaoToken(code: String): KakaoTokenResponse {
        val headers = HttpHeaders().apply {
            set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8")
        }
        val params = LinkedMultiValueMap<String, String>().apply {
            add("grant_type",   "authorization_code")
            add("client_id",    restApiKey)
            add("redirect_uri", redirectUri)
            add("code",         code)
        }

        return runCatching {
            restTemplate.postForObject(
                "https://kauth.kakao.com/oauth/token",
                HttpEntity(params, headers),
                KakaoTokenResponse::class.java
            )!!
        }.getOrElse {
            log.error("[Kakao OAuth] 토큰 발급 실패 - code: {}, error: {}", code, it.message, it)
            throw ZoniException(ErrorCode.OAUTH_LOGIN_FAILED)
        }
    }

    /**
     * [Step 2] 카카오 AccessToken → 사용자 정보 조회
     * GET https://kapi.kakao.com/v2/user/me
     *
     * ※ RestTemplate GET에 헤더를 실어야 할 때는 exchange() 사용
     *    (getForObject는 헤더를 추가할 수 없음)
     */
    private fun getKakaoUserInfo(kakaoAccessToken: String): KakaoUserInfoResponse {
        val headers = HttpHeaders().apply {
            set("Authorization", "Bearer $kakaoAccessToken")
            set("Content-Type",  "application/x-www-form-urlencoded;charset=utf-8")
        }

        return runCatching {
            restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                HttpEntity<String>(headers),
                KakaoUserInfoResponse::class.java
            ).body ?: throw Exception("카카오 사용자 정보 응답이 비어 있음")
        }.getOrElse {
            log.error("[Kakao OAuth] 사용자 정보 조회 실패: {}", it.message)
            throw ZoniException(ErrorCode.OAUTH_LOGIN_FAILED)
        }
    }

    // ── 회원 처리 ────────────────────────────────────────────────────────

    /**
     * 카카오 사용자 정보로 회원 찾기 또는 신규 가입
     *
     * 처리 순서:
     * Case 1. oauthId로 기존 회원 조회 → 있으면 프로필 업데이트 후 반환
     * Case 2. 이메일로 조회 → 이메일 로그인 계정이 있으면 카카오 연동
     * Case 3. 둘 다 없으면 신규 자동 가입
     */
    private fun findOrCreateUser(userInfo: KakaoUserInfoResponse): User {
        val oauthId  = userInfo.id.toString()
        val email    = userInfo.getEmail() ?: "$oauthId@kakao.local"
        val nickname = userInfo.getNickname()
        val imageUrl = userInfo.getProfileImageUrl()

        // Case 1: 카카오 oauthId로 기존 회원 찾기
        val existingUserByOauth = userRepository.findByOauthId(oauthId).orElse(null)
        if (existingUserByOauth != null) {
            existingUserByOauth.nickname        = nickname
            existingUserByOauth.profileImageUrl = imageUrl
            return userRepository.save(existingUserByOauth)
        }

        // Case 2: 같은 이메일로 가입된 일반 회원 → 카카오 연동
        val existingUserByEmail = userRepository.findByEmail(email).orElse(null)
        if (existingUserByEmail != null) {
            existingUserByEmail.oauthProvider   = OAuthProvider.KAKAO
            existingUserByEmail.oauthId         = oauthId
            existingUserByEmail.profileImageUrl = imageUrl
            return userRepository.save(existingUserByEmail)
        }

        // Case 3: 완전 신규 → 자동 가입
        log.info("[Kakao OAuth] 신규 회원 자동 가입 - email: {}", email)
        return userRepository.save(
            User(
                email           = email,
                password        = null,
                nickname        = nickname,
                oauthProvider   = OAuthProvider.KAKAO,
                oauthId         = oauthId,
                profileImageUrl = imageUrl
            )
        )
    }
}
