package io.github.bernardusz.`healthcare-billing`.auth

import io.github.bernardusz.`healthcare-billing`.auth.dto.LoginRequest
import io.github.bernardusz.`healthcare-billing`.auth.dto.LoginResponse
import io.github.bernardusz.`healthcare-billing`.auth.dto.RegisterRequest
import io.github.bernardusz.`healthcare-billing`.auth.service.JwtService
import io.github.bernardusz.`healthcare-billing`.auth.service.RefreshTokenService
import io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.internal.InternalServerException
import io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.missing.UserNotFoundException
import io.github.bernardusz.`healthcare-billing`.shared.utils.encodeNonNull
import io.github.bernardusz.`healthcare-billing`.user.User
import io.github.bernardusz.`healthcare-billing`.user.UserRepository
import io.github.bernardusz.`healthcare-billing`.user.dto.UserSecurity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


@Service
class AuthService (
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val authenticationManager: ReactiveAuthenticationManager,
    private val jwtService: JwtService,
    private val refreshTokenService: RefreshTokenService
) {
    @Transactional
    suspend fun registerUser(registerRequest: RegisterRequest): Long {
        val hashedPassword = passwordEncoder.encode(registerRequest.password)
            ?: throw InternalServerException(
                "Failed to register user - Password Hashing Error."
            )

        val newUser = User(
            username = registerRequest.username,
            email = registerRequest.email,
            password = hashedPassword,
        )

        return userRepository.save(newUser).id
            ?: throw IllegalStateException("Database failed to generate user ID")
    }

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
        val userId = userSecurity.getId()

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

    fun getUserTokens(userId: Long): Flow<RefreshToken> {
        return refreshTokenService.getUserTokens(userId)
    }

    @Transactional
    suspend fun revokeAllUserSessions(userId: Long): Int {
        return refreshTokenService.revokeAllUserSessions(userId)
    }

    @Transactional
    suspend fun getUserCount(): Long {
        return userRepository.count()
    }

    @Transactional
    suspend fun createInitialAdminAccount(
        registerRequest: RegisterRequest
    ): Long {
        val hashedPassword: String = passwordEncoder.encodeNonNull(
            registerRequest.password
        )

        val newUser = User(
            username = registerRequest.username,
            email = registerRequest.email,
            password = hashedPassword,
            role = "ROLE_ADMIN",
        )

        return userRepository.save(newUser).id
            ?: throw IllegalStateException("Database failed to generate user ID")
    }
}