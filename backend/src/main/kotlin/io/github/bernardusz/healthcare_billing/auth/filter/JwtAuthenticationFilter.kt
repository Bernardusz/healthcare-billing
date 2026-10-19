package io.github.bernardusz.healthcare_billing.auth.filter

import io.github.bernardusz.healthcare_billing.auth.service.CustomUserDetailsService
import io.github.bernardusz.healthcare_billing.auth.service.JwtService
import kotlinx.coroutines.withContext
import kotlinx.coroutines.reactor.asCoroutineContext
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.server.CoWebFilter
import org.springframework.web.server.CoWebFilterChain
import org.springframework.web.server.ServerWebExchange

@Component
class JwtAuthenticationFilter(
    private val jwtService: JwtService,
    private val customUserDetailsService: CustomUserDetailsService
) : CoWebFilter() {

    override suspend fun filter(
        exchange: ServerWebExchange,
        chain: CoWebFilterChain
    ) {
        val path = exchange.request.uri.path

        // Skip filter for auth endpoints
        if (isExcludedPath(path)) {
            chain.filter(exchange)
            return
        }

        // Extract token via Authorization Bearer or HttpCookie reactively
        val jwt = extractToken(exchange)

        if (jwt.isNullOrBlank()) {
            chain.filter(exchange)
            return
        }

        try {
            val userId = jwtService.extractSubjectId(jwt)

            if (userId != null) {
                // Non-blocking suspend call to load user from R2DBC
                val userSecurity = customUserDetailsService.loadUserById(userId)

                if (jwtService.validateToken(jwt, userSecurity)) {
                    val authToken = UsernamePasswordAuthenticationToken(
                        userSecurity,
                        null,
                        userSecurity.authorities
                    )

                    // Attach authentication to Reactive Security Context
                    val securityContext = ReactiveSecurityContextHolder
                        .withAuthentication(authToken).asCoroutineContext()

                    // Continue filter chain with context attached
                    withContext(securityContext) {
                        chain.filter(exchange)
                    }
                    return
                }
            }
        } catch (e: Exception) {
            // In WebFlux, rethrowing or passing down allows ServerAuthenticationEntryPoint
            // or WebExceptionHandler to handle it cleanly.
            throw e
        }

        chain.filter(exchange)
    }

    private fun isExcludedPath(path: String): Boolean {
        return path == "/api/v1/auth/login"
                || path == "/api/v1/auth/register"
                || path == "/api/v1/auth/refresh"
                || path == "/api/v1/auth/logout"
    }

    private fun extractToken(exchange: ServerWebExchange): String? {
        // 1. Check Authorization header
        val authHeader = exchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7)
        }

        // 2. Fallback to HttpCookie if present
        return exchange.request.cookies.getFirst("AUTH-TOKEN")?.value
    }
}