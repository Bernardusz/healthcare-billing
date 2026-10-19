package io.github.bernardusz.healthcare_billing.user.dto

data class UserUpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)
