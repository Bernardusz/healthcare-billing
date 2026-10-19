package io.github.bernardusz.healthcare_billing.user.dto

import io.github.bernardusz.healthcare_billing.user.UserRole

data class UserCreateRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.ROLE_BILLER,
)