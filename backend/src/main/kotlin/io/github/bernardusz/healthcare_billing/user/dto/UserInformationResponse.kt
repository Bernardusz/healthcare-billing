package io.github.bernardusz.healthcare_billing.user.dto

import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing.UserNotFoundException
import io.github.bernardusz.healthcare_billing.user.User
import io.github.bernardusz.healthcare_billing.user.UserRole
import java.time.Instant

data class UserInformationResponse(
    val id: Long,
    val username: String,
    val email: String,
    val role: UserRole,
    val createdAt: Instant,
)

fun User.toInformationResponse(): UserInformationResponse = UserInformationResponse(
    id = this.id ?: throw UserNotFoundException("User id is null"),
    username = this.username,
    email = this.email,
    role = this.role,
    createdAt = this.createdAt,
)