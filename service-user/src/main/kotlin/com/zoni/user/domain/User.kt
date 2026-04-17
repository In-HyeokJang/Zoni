package com.zoni.user.domain

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
class User(

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    val email: String,

    /** OAuth 로그인 시 비밀번호 없음 → nullable */
    @Column
    var password: String? = null,

    @Column(nullable = false)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: UserRole = UserRole.USER,

    /**
     * OAuth 제공자 (KAKAO, GOOGLE 등)
     * 일반 이메일 로그인 시 null
     * Phase 1 카카오 OAuth 구현 예정
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider")
    var oauthProvider: OAuthProvider? = null,

    /**
     * OAuth 제공자의 사용자 고유 ID
     * 카카오 로그인 시 카카오 userId 저장
     */
    @Column(name = "oauth_id", unique = true)
    var oauthId: String? = null,

    /**
     * 프로필 이미지 URL
     * 카카오 로그인 시 카카오 프로필 이미지 자동 저장
     */
    @Column(name = "profile_image_url", length = 500)
    var profileImageUrl: String? = null,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)

enum class UserRole {
    USER, ADMIN
}

enum class OAuthProvider {
    KAKAO,   // 카카오 로그인 (Phase 1)
    GOOGLE   // 구글 로그인 (미래 확장)
}
