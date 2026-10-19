package io.github.bernardusz.`healthcare-billing`.auth.service

import io.github.bernardusz.`healthcare-billing`.auth.AuthRepository
import io.github.bernardusz.`healthcare-billing`.auth.RefreshToken
import io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.external.RefreshTokenException
import kotlinx.coroutines.flow.Flow
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.HexFormat
import java.util.UUID

@Service
class RefreshTokenService (
    private val authRepository: AuthRepository,
    @Value("\${jwt.refresh-token-expiration-ms}") private val refreshTokenExpirationMs: Long
) {
    fun hashToken(rawToken: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(rawToken.toByteArray())
        return HexFormat.of().formatHex(hashBytes)
    }

    @Transactional
    suspend fun createRefreshToken(userId: Long): String {
        val rawUuid = UUID.randomUUID().toString()
        val hashedToken: String = hashToken(rawUuid)

        val refreshToken = RefreshToken(
            userId = userId,
            tokenHash = hashedToken,
            expiresAt = Instant.now().plusMillis(refreshTokenExpirationMs),
            revoked = false
        )

        authRepository.save(refreshToken)
        return rawUuid
    }

    @Transactional(readOnly = true)
    suspend fun findValidToken(rawToken: String): RefreshToken {
        val hashedToken = hashToken(rawToken)
        val token: RefreshToken = authRepository.findByTokenHash(hashedToken)
            ?: throw RefreshTokenException(
                "No refresh token found", code = 404)

        if (token.revoked){
            throw RefreshTokenException("Access token had been revoked", code = 401)
        }

        if (token.expiresAt.isBefore(Instant.now())){
            throw RefreshTokenException("Access token has expired", code = 401)
        }

        return token
    }

    @Transactional(readOnly = true)
    fun getUserTokens(userId: Long): Flow<RefreshToken> {
        return authRepository.findAllByUserId(userId)
    }

    @Transactional
    suspend fun revokeToken(rawToken: String) {
        val hashedToken = hashToken(rawToken)
        val refreshToken: RefreshToken = authRepository.findByTokenHash(hashedToken)
            ?: throw RefreshTokenException(
                "No refresh token found", code = 401
            )
        val revokedToken = refreshToken.copy(revoked = true)

        authRepository.save(revokedToken)
    }

    @Transactional
    suspend fun revokeAllUserSessions(userId: Long): Int {
        return authRepository.revokeAllByUserId(userId)
    }

    @Transactional
    suspend fun purgeExpiredTokensOrRevokedTokens(): Int {
        return authRepository.deleteAllExpiredOrRevoked()
    }

    fun getRefreshTokenExpiration(): Long {
        return refreshTokenExpirationMs
    }
}