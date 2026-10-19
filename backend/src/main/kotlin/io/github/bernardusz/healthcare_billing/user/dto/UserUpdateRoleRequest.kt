package io.github.bernardusz.healthcare_billing.user.dto

import io.github.bernardusz.healthcare_billing.user.UserRole

data class UserUpdateRoleRequest(
    val role: UserRole = UserRole.ROLE_BILLER,
)
