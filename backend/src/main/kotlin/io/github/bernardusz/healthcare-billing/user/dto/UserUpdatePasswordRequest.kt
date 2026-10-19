package io.github.bernardusz.`healthcare-billing`.user.dto

data class UserUpdatePasswordRequest(
    val currentPassword: String,
    val newPassword: String,
)
