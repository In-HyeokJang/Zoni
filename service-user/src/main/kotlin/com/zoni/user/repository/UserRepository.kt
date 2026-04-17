package com.zoni.user.repository

import com.zoni.user.domain.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun existsByEmail(email: String): Boolean

    /** 카카오 OAuth 로그인 시 사용 - oauthId로 기존 회원 조회 */
    fun findByOauthId(oauthId: String): Optional<User>

}