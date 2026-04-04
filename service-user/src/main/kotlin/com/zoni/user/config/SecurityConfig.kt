package com.zoni.user.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring MVC 기반 Security 설정
 *
 * ※ WebFlux Security와의 차이점:
 *   - MVC: HttpSecurity, OncePerRequestFilter, SecurityFilterChain
 *   - WebFlux: ServerHttpSecurity, WebFilter, SecurityWebFilterChain
 *
 * 현재는 MVC로 구현 → 추후 service-chat, service-feed 등에 WebFlux 적용 예정
 */
@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter  // JWT 필터 주입
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }          // REST API는 CSRF 불필요
            .formLogin { it.disable() }     // 폼 로그인 미사용
            .httpBasic { it.disable() }     // Basic 인증 미사용
            .sessionManagement {
                // JWT 사용 → 세션 완전 비활성화 (Stateless)
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
            .authorizeHttpRequests {
                // 인증 없이 접근 허용할 엔드포인트
                it.requestMatchers(
                    "/api/users/signup",
                    "/api/users/login",
                    "/api/users/refresh",   // refresh token은 자체가 인증 수단이므로 허용
                    "/health",
                    "/actuator/**"
                ).permitAll()
                // 그 외 모든 요청은 인증 필요
                it.anyRequest().authenticated()
            }
            // UsernamePasswordAuthenticationFilter 앞에 JWT 필터 등록
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
