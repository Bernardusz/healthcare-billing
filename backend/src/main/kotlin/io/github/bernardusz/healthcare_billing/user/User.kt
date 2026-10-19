package io.github.bernardusz.healthcare_billing.user

import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

enum class UserRole {
    ROLE_ADMIN,
    ROLE_AUDITOR,
    ROLE_BILLER
}

@Table("users")
data class User(
    @Id val id: Long? = null,
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole = UserRole.ROLE_BILLER,
    val createdAt: Instant = Instant.now(),
)
