package com.zoni.place.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

/**
 * service-place Spring Security 설정
 *
 * 비로그인 허용 API:
 *   GET /api/places/search     키워드 검색
 *   GET /api/places/popular    인기 장소
 *   GET /api/places/{숫자id}   상세 조회
 *   GET /api/places/benefits   혜택 장소 목록 (Phase 3-B 신설)
 *
 * 나머지 모든 API는 JWT 인증 필요.
 */
@Configuration
class SecurityConfig(
    private val jwtAuthFilter: JwtAuthFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests {
                it.requestMatchers("/health", "/actuator/**").permitAll()
                it.requestMatchers("GET", "/api/places/search").permitAll()
                it.requestMatchers("GET", "/api/places/popular").permitAll()
                it.requestMatchers("GET", "/api/places/{id:[0-9]+}").permitAll()
                // [Phase 3-B] 혜택 장소 목록 비로그인 허용
                it.requestMatchers("GET", "/api/places/benefits").permitAll()
                it.anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf("http://localhost:3000", "http://localhost:3001", "http://localhost:8080")
        config.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
