package com.zoni.user.dto.response

import com.fasterxml.jackson.annotation.JsonProperty

/** 카카오 토큰 발급 응답 */
data class KakaoTokenResponse(
    @JsonProperty("access_token")  val accessToken: String,
    @JsonProperty("token_type")    val tokenType: String,
    @JsonProperty("refresh_token") val refreshToken: String? = null,
    @JsonProperty("expires_in")    val expiresIn: Int = 0
)

/** 카카오 사용자 정보 응답 */
data class KakaoUserInfoResponse(
    val id: Long,

    @JsonProperty("kakao_account")
    val kakaoAccount: KakaoAccount? = null
) {
    data class KakaoAccount(
        val email: String? = null,

        @JsonProperty("email_needs_agreement")
        val emailNeedsAgreement: Boolean? = null,

        val profile: Profile? = null
    )

    data class Profile(
        val nickname: String? = null,

        @JsonProperty("profile_image_url")
        val profileImageUrl: String? = null,

        @JsonProperty("is_default_image")
        val isDefaultImage: Boolean? = null
    )

    /** 닉네임 편의 메서드 */
    fun getNickname(): String =
        kakaoAccount?.profile?.nickname ?: "카카오유저"

    /** 프로필 이미지 편의 메서드 */
    fun getProfileImageUrl(): String? =
        kakaoAccount?.profile?.profileImageUrl
            ?.takeIf { kakaoAccount.profile?.isDefaultImage != true }

    /** 이메일 편의 메서드 */
    fun getEmail(): String? = kakaoAccount?.email
}

