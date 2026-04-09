package com.zoni.user.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.user.config.JwtProvider
import com.zoni.user.domain.User
import com.zoni.user.dto.request.LoginRequest
import com.zoni.user.dto.request.SignUpRequest
import com.zoni.user.dto.request.TokenRefreshRequest
import com.zoni.user.dto.response.LoginResponse
import com.zoni.user.dto.response.UserResponse
import com.zoni.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider,
    private val refreshTokenService: RefreshTokenService
) {
    @Transactional
    fun signUp(request: SignUpRequest): Long {
        if (userRepository.existsByEmail(request.email)) {
            throw ZoniException(ErrorCode.DUPLICATE_EMAIL)
        }
        val user = User(
            email = request.email,
            password = passwordEncoder.encode(request.password),
            nickname = request.nickname
        )
        return userRepository.save(user).id
    }

    fun login(request: LoginRequest): LoginResponse {
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }

        if (!passwordEncoder.matches(request.password, user.password)) {
            throw ZoniException(ErrorCode.UNAUTHORIZED)
        }

        val accessToken = jwtProvider.generateToken(user.id, user.email, user.nickname)
        val refreshToken = jwtProvider.generateRefreshToken(user.id, user.email, user.nickname)

        // Redis에 refresh token 저장 (기존 값 덮어쓰기 → 중복 로그인 방지)
        refreshTokenService.save(user.id, refreshToken)

        return LoginResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            userId = user.id,
            email = user.email,
            nickname = user.nickname
        )
    }

    /**
     * Access Token 재발급
     * 1. refresh token JWT 유효성 검증
     * 2. Redis 저장값과 비교 (탈취 방어)
     * 3. 새 access token + refresh token 발급 (Token Rotation)
     */
    fun refresh(request: TokenRefreshRequest): LoginResponse {
        val refreshToken = request.refreshToken

        // JWT 서명/만료 검증
        if (!jwtProvider.isValid(refreshToken)) {
            throw ZoniException(ErrorCode.INVALID_TOKEN)
        }

        val userId = jwtProvider.getUserId(refreshToken)
        val email  = jwtProvider.getEmail(refreshToken)

        // Redis 저장값과 일치 여부 검증
        if (!refreshTokenService.isValid(userId, refreshToken)) {
            throw ZoniException(ErrorCode.INVALID_TOKEN)
        }

        val user = userRepository.findByEmail(email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }

        // Token Rotation: 재발급 시 refresh token도 새로 발급해서 Redis 갱신
        val newAccessToken  = jwtProvider.generateToken(userId, email, user.nickname)
        val newRefreshToken = jwtProvider.generateRefreshToken(userId, email, user.nickname)
        refreshTokenService.save(userId, newRefreshToken)

        return LoginResponse(
            accessToken  = newAccessToken,
            refreshToken = newRefreshToken,
            userId       = user.id,
            email        = user.email,
            nickname     = user.nickname
        )
    }

    /**
     * 로그아웃
     * 1. Redis에서 refresh token 삭제 → 재발급 불가
     * 2. Access token 블랙리스트 등록 → 남은 만료시간 동안 사용 차단
     */
    fun logout(email: String, accessToken: String) {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }
        refreshTokenService.delete(user.id)
        val remainingMs = jwtProvider.getRemainingExpiration(accessToken)
        refreshTokenService.addToBlacklist(accessToken, remainingMs)
    }

    /** 내 정보 조회 (JWT 인증 통과한 사용자만 호출 가능) */
    fun getMe(email: String): UserResponse {
        val user = userRepository.findByEmail(email)
            .orElseThrow { ZoniException(ErrorCode.USER_NOT_FOUND) }
        return UserResponse(
            userId   = user.id,
            email    = user.email,
            nickname = user.nickname,
            role     = user.role.name
        )
    }
}