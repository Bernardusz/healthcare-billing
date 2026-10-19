package io.github.bernardusz.`healthcare-billing`.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("users")
data class User(
    @Id val id: Long? = null,
    val username: String,
    val email: String,
    val password: String,
    val role: String = "ROLE_USER",
    val createdAt: Instant = Instant.now(),
)
