package io.github.bernardusz.healthcare_billing.auth

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("refresh_tokens")
data class RefreshToken(
    @Id val id: Long? = null,
    val userId: Long,
    val tokenHash: String,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now(),
    val revoked: Boolean = false
)