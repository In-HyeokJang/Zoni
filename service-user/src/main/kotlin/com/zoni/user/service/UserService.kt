package com.zoni.user.service

import com.zoni.common.ErrorCode
import com.zoni.common.ZoniException
import com.zoni.user.config.JwtProvider
import com.zoni.user.domain.User
import com.zoni.user.dto.request.LoginRequest
import com.zoni.user.dto.request.SignUpRequest
import com.zoni.user.dto.response.LoginResponse
import com.zoni.user.repository.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtProvider: JwtProvider
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
        val token = jwtProvider.generateToken(user.id, user.email)

        return LoginResponse(
            accessToken = token,
            userId = user.id,
            email = user.email,
            nickname = user.nickname
        )
    }
}