package io.github.bernardusz.`healthcare-billing`.user.dto

data class UserUpdateRoleRequest(
    val role: String = "ROLE_USER",
)
