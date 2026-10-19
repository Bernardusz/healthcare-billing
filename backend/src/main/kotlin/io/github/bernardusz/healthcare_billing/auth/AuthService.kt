package io.github.bernardusz.healthcare_billing.auth

import io.github.bernardusz.healthcare_billing.auth.dto.LoginRequest
import io.github.bernardusz.healthcare_billing.auth.dto.LoginResponse
import io.github.bernardusz.healthcare_billing.auth.service.JwtService
import io.github.bernardusz.healthcare_billing.auth.service.RefreshTokenService
import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing.UserNotFoundException
import io.github.bernardusz.healthcare_billing.user.User
import io.github.bernardusz.healthcare_billing.user.UserRepository
import io.github.bernardusz.healthcare_billing.user.dto.UserSecurity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class AuthService (
    private val userRepository: UserRepository,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService
) {
    @Transactional
    suspend fun loginUser(loginRequest: LoginRequest): LoginResponse{
        val authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(
                loginRequest.identifier,
                loginRequest.password
            )
        ).awaitSingle()

        val userSecurity = authentication.principal as UserSecurity
        val user = userSecurity.user
        val userId = userSecurity.id

        val newAccessToken = jwtService.generateToken(UserSecurity(user))
        val refreshToken: String = refreshTokenService.createRefreshToken(userId)

        return LoginResponse(
            accessToken = newAccessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = jwtService.getAccessTokenExpiration(),
            refreshTokenExpiresIn = refreshTokenService.getRefreshTokenExpiration()
        )
    }

    @Transactional
    suspend fun refreshAccessToken(refreshToken: String): LoginResponse{
        val refreshTokenObject: RefreshToken =
            refreshTokenService.findValidToken(
            refreshToken
        )

        val user: User = userRepository.findById(refreshTokenObject.userId)
            ?: throw UserNotFoundException("User with id ${refreshTokenObject.userId} not found")
        val userId = user.id ?: throw UserNotFoundException(
            "User with id ${refreshTokenObject.userId} not found"
        )

        val newAccessToken = jwtService.generateToken(UserSecurity(user))
        val newRefreshToken: String = refreshTokenService.createRefreshToken(userId)
        refreshTokenService.revokeToken(refreshToken)

        return LoginResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
            accessTokenExpiresIn = jwtService.getAccessTokenExpiration(),
            refreshTokenExpiresIn = refreshTokenService.getRefreshTokenExpiration()
        )
    }
}