package io.github.bernardusz.`healthcare-billing`.auth.dto

import io.github.bernardusz.`healthcare-billing`.auth.RefreshToken
import java.time.Instant

data class RefreshTokenSessionResponse(
    val id: Long? = null,
    val userId: Long,
    val expiresAt: Instant,
    val createdAt: Instant = Instant.now(),
)

fun RefreshToken.toTokenSessionResponse(): RefreshTokenSessionResponse = RefreshTokenSessionResponse(
    id = this.id,
    userId = this.userId,
    expiresAt = this.expiresAt,
    createdAt = this.createdAt,
)