package io.github.bernardusz.healthcare_billing.auth.config

import io.github.bernardusz.healthcare_billing.auth.filter.JwtAuthenticationFilter
import io.github.bernardusz.healthcare_billing.auth.service.CustomUserDetailsService
import io.github.bernardusz.healthcare_billing.shared.exception.CustomAccessDeniedHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.reactive.CorsConfigurationSource
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customUserDetailsService: CustomUserDetailsService
) {
    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        customAccessDeniedHandler: CustomAccessDeniedHandler
    ): SecurityWebFilterChain {
        return http
            .csrf { csrf -> csrf.disable() }
            .cors { cors ->
                cors.configurationSource(
                    corsConfigurationSource()
                )
            }
            .exceptionHandling { exceptionHandling ->
                exceptionHandling.accessDeniedHandler(customAccessDeniedHandler)
            }
            .authorizeExchange { exchange ->
                exchange.pathMatchers(
                    "/api/v1/auth/login",
                    "/api/v1/auth/register",
                    "/api/v1/auth/refresh",
                    "/api/v1/auth/me",
                    "/api/v1/auth/logout",
                    "/error",
                    "/api/v1/auth/setup-status",
                    "/api/v1/auth/initial-setup",
                    "/v3/api-docs/**",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/webjars/**"
                ).permitAll()
                .anyExchange().authenticated()
            }
            .authenticationManager (reactiveAuthenticationManager())
            .addFilterAt(
                jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION
            )
            .build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "http://localhost:5173",
            "https://localhost:5173"
        )
        configuration.allowedMethods = listOf(
            "GET", "POST", "PUT", "DELETE", "OPTIONS"
        )
        configuration.allowedHeaders = listOf(
            "Authorization",
            "Content-Type",
            "Cookie"
        )
        configuration.allowCredentials = true

        // IMPORTANT: Use org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    fun reactiveAuthenticationManager():
            ReactiveAuthenticationManager {
        val manager = UserDetailsRepositoryReactiveAuthenticationManager(
            customUserDetailsService
        )
        manager.setPasswordEncoder(passwordEncoder())
        return manager
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
}