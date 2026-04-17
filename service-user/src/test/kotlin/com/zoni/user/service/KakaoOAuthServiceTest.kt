package com.zoni.user.service

import com.zoni.user.config.JwtProvider
import com.zoni.user.domain.OAuthProvider
import com.zoni.user.domain.User
import com.zoni.user.dto.response.KakaoUserInfoResponse
import com.zoni.user.dto.response.KakaoUserInfoResponse.KakaoAccount
import com.zoni.user.dto.response.KakaoUserInfoResponse.Profile
import com.zoni.user.repository.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.client.RestTemplate
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class KakaoOAuthServiceTest {

    @Mock lateinit var userRepository: UserRepository
    @Mock lateinit var jwtProvider: JwtProvider
    @Mock lateinit var refreshTokenService: RefreshTokenService
    @Mock lateinit var restTemplate: RestTemplate

    // @Value 필드가 있어서 @InjectMocks 불가 → BeforeEach에서 직접 생성
    lateinit var kakaoOAuthService: KakaoOAuthService

    @BeforeEach
    fun setUp() {
        kakaoOAuthService = KakaoOAuthService(
            userRepository    = userRepository,
            jwtProvider       = jwtProvider,
            refreshTokenService = refreshTokenService,
            restTemplate      = restTemplate,
            restApiKey        = "test-api-key",
            redirectUri       = "http://localhost:8081/api/users/login/kakao/callback",
            clientSecret      = "test-client-secret"
        )
    }

    // ── 테스트 픽스처 ────────────────────────────────────────────────

    private fun fakeUserInfo(id: Long = 123L, email: String? = "test@kakao.com") =
        KakaoUserInfoResponse(
            id = id,
            kakaoAccount = KakaoAccount(
                email = email,
                profile = Profile(nickname = "테스트유저", profileImageUrl = "https://img.kakao.com/profile.jpg")
            )
        )

    private fun existingKakaoUser() = User(
        email         = "test@kakao.com",
        nickname      = "기존유저",
        oauthId       = "123",
        oauthProvider = OAuthProvider.KAKAO
    )

    private fun existingEmailUser() = User(
        email         = "test@kakao.com",
        nickname      = "일반유저",
        oauthProvider = null
    )

    // ── 테스트 케이스 ────────────────────────────────────────────────

    @Test
    fun `신규 유저면 자동 가입 후 KAKAO provider로 반환`() {
        given(userRepository.findByOauthId(any())).willReturn(Optional.empty())
        given(userRepository.findByEmail(any())).willReturn(Optional.empty())
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = kakaoOAuthService.findOrCreateUser(fakeUserInfo())

        assertThat(result.oauthProvider).isEqualTo(OAuthProvider.KAKAO)
        assertThat(result.oauthId).isEqualTo("123")
        assertThat(result.email).isEqualTo("test@kakao.com")
        verify(userRepository).save(any())
    }

    @Test
    fun `기존 카카오 유저면 save 1번만 호출하고 oauthId 유지`() {
        given(userRepository.findByOauthId("123")).willReturn(Optional.of(existingKakaoUser()))
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = kakaoOAuthService.findOrCreateUser(fakeUserInfo())

        // oauthId로 찾았으니 email 조회는 하지 않아야 함
        verify(userRepository, never()).findByEmail(any())
        assertThat(result.oauthId).isEqualTo("123")
    }

    @Test
    fun `동일 이메일 일반 유저면 카카오 연동 후 KAKAO provider로 반환`() {
        given(userRepository.findByOauthId(any())).willReturn(Optional.empty())
        given(userRepository.findByEmail("test@kakao.com")).willReturn(Optional.of(existingEmailUser()))
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = kakaoOAuthService.findOrCreateUser(fakeUserInfo())

        assertThat(result.oauthProvider).isEqualTo(OAuthProvider.KAKAO)
        assertThat(result.oauthId).isEqualTo("123")
    }

    @Test
    fun `카카오 이메일 없으면 oauthId@kakao_local 이메일로 가입`() {
        given(userRepository.findByOauthId(any())).willReturn(Optional.empty())
        given(userRepository.findByEmail(any())).willReturn(Optional.empty())
        whenever(userRepository.save(any())).thenAnswer { it.arguments[0] as User }

        val result = kakaoOAuthService.findOrCreateUser(fakeUserInfo(email = null))

        assertThat(result.email).isEqualTo("123@kakao.local")
    }
}