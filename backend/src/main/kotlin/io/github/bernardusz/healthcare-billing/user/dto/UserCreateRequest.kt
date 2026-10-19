package io.github.bernardusz.`healthcare-billing`.user.dto

data class UserCreateRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String = "ROLE_USER",
)