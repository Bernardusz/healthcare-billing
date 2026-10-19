package io.github.bernardusz.healthcare_billing.auth

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Modifying
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface AuthRepository : CoroutineCrudRepository<RefreshToken, Long> {
    // Returns all active, unrevoked tokens for a given user
    fun findAllByUserIdAndRevokedFalse(userId: Long): Flow<RefreshToken>
    // Returns a single active token matching the SHA-256 hash
    suspend fun findByTokenHash(tokenHash: String): RefreshToken?

    @Modifying
    @Query("UPDATE refresh_tokens SET revoked = true WHERE user_id = :userId AND revoked = false")
    suspend fun revokeAllByUserId(userId: Long): Int

    // Delete expired tokens to keep the table size small (Scheduled cleanup job)
    @Modifying
    @Query("DELETE FROM refresh_tokens WHERE expires_at < CURRENT_TIMESTAMP OR revoked = true")
    suspend fun deleteAllExpiredOrRevoked(): Int
}