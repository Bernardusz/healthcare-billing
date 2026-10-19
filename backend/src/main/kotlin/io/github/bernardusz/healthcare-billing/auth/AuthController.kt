package io.github.bernardusz.`healthcare-billing`.auth

import io.github.bernardusz.`healthcare-billing`.auth.dto.LoginRequest
import io.github.bernardusz.`healthcare-billing`.auth.dto.LoginResponse
import io.github.bernardusz.`healthcare-billing`.auth.dto.RefreshTokenSessionResponse
import io.github.bernardusz.`healthcare-billing`.auth.dto.RegisterRequest
import io.github.bernardusz.`healthcare-billing`.auth.dto.UserSessionResponse
import io.github.bernardusz.`healthcare-billing`.auth.dto.toTokenSessionResponse
import io.github.bernardusz.`healthcare-billing`.auth.service.RefreshTokenService
import io.github.bernardusz.`healthcare-billing`.shared.utils.createAccessTokenCookie
import io.github.bernardusz.`healthcare-billing`.shared.utils.createRefreshTokenCookie
import io.github.bernardusz.`healthcare-billing`.user.dto.UserInformationResponse
import io.github.bernardusz.`healthcare-billing`.user.dto.UserSecurity
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.CookieValue
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val authService: AuthService,
    private val refreshTokenService: RefreshTokenService,
) {
    @Volatile
    private var isSystemInitialized: Boolean = false

    @GetMapping("/me")
    suspend fun getCurrentUser(
        @AuthenticationPrincipal user: UserSecurity?
    ): ResponseEntity<UserSessionResponse> {
        if (user == null) {
            return ResponseEntity.ok(
                UserSessionResponse(
                    isAuthenticated = false,
                )
            )
        }

        val userInfo = UserInformationResponse(
            id = user.getId(),
            username = user.username,
            email = user.getEmail(),
            role = user.getRole(),
            createdAt = user.getCreatedAt()
        )

        return ResponseEntity.ok(
            UserSessionResponse(
                isAuthenticated = true,
                user = userInfo
            )
        )
    }

    @PostMapping("/login")
    suspend fun login(
        @Valid @RequestBody loginRequest: LoginRequest,
        response: ServerHttpResponse
    ): ResponseEntity<Void> {
        val loginResponse: LoginResponse =
            authService.loginUser(loginRequest)

        val accessTokenCookie: ResponseCookie =
            createAccessTokenCookie(
                loginResponse.accessToken,
                loginResponse.accessTokenExpiresIn
            )

        val refreshTokenCookie: ResponseCookie =
            createRefreshTokenCookie(
                loginResponse.refreshToken,
                loginResponse.refreshTokenExpiresIn
            )

        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)

        return ResponseEntity.ok().build()
    }

    @PostMapping("/refresh")
    suspend fun refreshAccessToken(
        @CookieValue(
            name = "REFRESH-TOKEN",
            required = false
        ) refreshToken: String?,
        response: ServerHttpResponse
    ): ResponseEntity<Void> {
        if (refreshToken.isNullOrBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val loginResponse: LoginResponse = authService.refreshAccessToken(refreshToken)

        val accessTokenCookie: ResponseCookie =
            createAccessTokenCookie(loginResponse.accessToken, loginResponse.accessTokenExpiresIn)

        val refreshTokenCookie: ResponseCookie =
            createRefreshTokenCookie(loginResponse.refreshToken, loginResponse.refreshTokenExpiresIn)

        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)

        return ResponseEntity.ok().build()
    }

    @PostMapping("/logout")
    suspend fun logout(
        @CookieValue(
            name = "REFRESH-TOKEN",
            required = false
        ) refreshToken: String?,
        response: ServerHttpResponse
    ): ResponseEntity<Void> {
        if (!refreshToken.isNullOrBlank()) {
            refreshTokenService.revokeToken(refreshToken)
        }

        val accessTokenCookie: ResponseCookie =
            createAccessTokenCookie("", 0)

        val refreshTokenCookie: ResponseCookie =
            createRefreshTokenCookie("", 0)

        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)

        return ResponseEntity.ok().build()
    }

    @PostMapping("/register")
    suspend fun register(
        @Valid @RequestBody registerRequest: RegisterRequest,
    ): ResponseEntity<Void> {
        val userId: Long = authService.registerUser(registerRequest)
        return ResponseEntity.created(
            URI.create("/api/v1/users/$userId")
        ).build()
    }

    // The only method in which users need to be authenticated
    @PostMapping("/logout-all")
    suspend fun logoutFromAllDevices(
        @AuthenticationPrincipal user: UserSecurity?,
        response: ServerHttpResponse
    ): ResponseEntity<Void> {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        authService.revokeAllUserSessions(
            userId = user.getId(),
        )

        val accessTokenCookie: ResponseCookie =
            createAccessTokenCookie("", 0)

        val refreshTokenCookie: ResponseCookie =
            createRefreshTokenCookie("", 0)

        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)

        return ResponseEntity.ok().build()
    }

    @GetMapping("/active-sessions")
     fun activeSessions(
        @AuthenticationPrincipal user: UserSecurity?,
    ): ResponseEntity<Flow<RefreshTokenSessionResponse>> {
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }

        val activeSessions: Flow<RefreshTokenSessionResponse> = authService.getUserTokens(
            userId = user.getId(),
        ).map {
            refreshToken -> refreshToken.toTokenSessionResponse()
        }

        return ResponseEntity.ok(
            activeSessions
        )
    }

    @GetMapping("/setup-status")
    suspend fun getSetupStatus(): ResponseEntity<Map<String, Boolean>> {
        if (isSystemInitialized) {
            return ResponseEntity.ok(mapOf("needsSetup" to false))
        }

        val userCount = authService.getUserCount()
        if (userCount > 0) {
            isSystemInitialized = true // Lock permanently in memory
            return ResponseEntity.ok(mapOf("needsSetup" to false))
        }

        return ResponseEntity.ok(mapOf("needsSetup" to true))
    }

    @PostMapping("/initial-setup")
    suspend fun createInitialAdmin(
        @Valid @RequestBody request: RegisterRequest
    ): ResponseEntity<Void> {
        if (isSystemInitialized || authService.getUserCount() > 0) {
            isSystemInitialized = true
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build()
        }

        authService.createInitialAdminAccount(request)
        isSystemInitialized = true
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }
}