package com.zoni.user.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.user.config.JwtProvider
import com.zoni.user.domain.User
import com.zoni.user.dto.request.BirthYearUpdateRequest
import com.zoni.user.dto.request.LoginRequest
import com.zoni.user.dto.request.SignUpRequest
import com.zoni.user.dto.request.TokenRefreshRequest
import com.zoni.user.dto.response.LoginResponse
import com.zoni.user.dto.response.UserResponse
import com.zoni.user.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val refreshTokenService: RefreshTokenService
) {
    private val log = LoggerFactory.getLogger(javaClass)

    /**
     * 회원가입
     *
     * 1. 이메일 중복 체크
     * 2. BCrypt 비밀번호 암호화
     * 3. 유저 저장 (birthYear 포함 — 선택 입력)
     *
     * @param request 회원가입 요청 (email, password, nickname, birthYear?)
     * @return 생성된 userId
     */
    @Transactional
    fun signUp(request: SignUpRequest): Long {
        if (userRepository.existsByEmail(request.email)) {
            throw ZoniException(ErrorCode.DUPLICATE_EMAIL)
        }
        val user = User(
            email     = request.email,
            password  = passwordEncoder.encode(request.password),
            nickname  = request.nickname,
            birthYear = request.birthYear
        )
        val saved = userRepository.save(user)
        log.info("[UserService] 회원가입 완료 - userId: {}, email: {}", saved.id, saved.email)
        return saved.id
    }

    /**
     * 이메일 로그인
     *
     * 1. 이메일로 유저 조회
     * 2. BCrypt 비밀번호 비교
     * 3. Access Token + Refresh Token 발급
     * 4. Refresh Token Redis 저장
     *
     * @return 토큰 및 사용자 기본 정보
     */
    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }

        if (!passwordEncoder.matches(request.password, user.password ?: throw ZoniException(ErrorCode.UNAUTHORIZED))) {
            throw ZoniException(ErrorCode.UNAUTHORIZED)
        }

        val accessToken  = jwtProvider.generateToken(user.id, user.email, user.nickname)
        val refreshToken = jwtProvider.generateRefreshToken(user.id, user.email, user.nickname)

        // Redis에 refresh token 저장 (기존 값 덮어쓰기 → 중복 로그인 방지)
        refreshTokenService.save(user.id, refreshToken)
        log.info("[UserService] 로그인 성공 - userId: {}, email: {}", user.id, user.email)

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

    /**
     * Access Token 재발급 (Token Rotation)
     *
     * 1. Refresh Token JWT 유효성 검증
     * 2. Redis 저장값과 비교 (탈취 방어)
     * 3. 새 Access Token + Refresh Token 동시 발급
     * 4. Redis의 Refresh Token 교체 (이전 토큰 즉시 무효화)
     */
    fun refresh(request: TokenRefreshRequest): LoginResponse {
        val refreshToken = request.refreshToken

        if (!jwtProvider.isValid(refreshToken)) {
            throw ZoniException(ErrorCode.INVALID_TOKEN)
        }

        val userId = jwtProvider.getUserId(refreshToken)
        val email  = jwtProvider.getEmail(refreshToken)

        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw ZoniException(ErrorCode.INVALID_TOKEN)
        }

        val user = userRepository.findByEmail(email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }

        val newAccessToken  = jwtProvider.generateToken(userId, email, user.nickname)
        val newRefreshToken = jwtProvider.generateRefreshToken(userId, email, user.nickname)
        refreshTokenService.save(userId, newRefreshToken)

        return LoginResponse(
            accessToken     = newAccessToken,
            refreshToken    = newRefreshToken,
            userId          = user.id,
            email           = user.email,
            nickname        = user.nickname,
            profileImageUrl = user.profileImageUrl,
            oauthProvider   = user.oauthProvider?.name
        )
    }

    /**
     * 로그아웃
     *
     * 1. Redis에서 Refresh Token 삭제 → 재발급 불가
     * 2. Access Token 블랙리스트 등록 → 남은 만료시간 동안 사용 차단
     */
    fun logout(email: String, accessToken: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }
        refreshTokenService.delete(user.id)
        val remainingMs = jwtProvider.getRemainingExpiration(accessToken)
        refreshTokenService.addToBlacklist(accessToken, remainingMs)
        log.info("[UserService] 로그아웃 완료 - userId: {}", user.id)
    }

    /**
     * 내 정보 조회
     *
     * JWT 인증을 통과한 사용자만 호출 가능하다.
     *
     * @param email JWT claim에서 추출한 이메일 (Principal.name)
     */
    fun getMe(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }
        return user.toResponse()
    }

    /**
     * 출생 연도 업데이트 (Phase 3-B: 청년 혜택 연령 필터용)
     *
     * 현재 연도보다 미래 연도는 등록 불가.
     * 기존 birthYear 가 있는 경우 덮어쓴다.
     *
     * @param email JWT claim에서 추출한 이메일
     * @param request 새로운 출생 연도
     */
    @Transactional
    fun updateBirthYear(email: String, request: BirthYearUpdateRequest) {
        val currentYear = LocalDate.now().year
        if (request.birthYear > currentYear) {
            throw ZoniException(ErrorCode.INVALID_BIRTH_YEAR)
        }
        val user = userRepository.findByEmail(email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }
        user.birthYear = request.birthYear
        log.info("[UserService] 출생 연도 업데이트 - userId: {}, birthYear: {}", user.id, request.birthYear)
    }

    private fun User.toResponse() = UserResponse(
        userId          = id,
        email           = email,
        nickname        = nickname,
        role            = role.name,
        profileImageUrl = profileImageUrl,
        oauthProvider   = oauthProvider?.name,
        birthYear       = birthYear
    )
}
