package io.github.bernardusz.healthcare_billing.user.dto

import io.github.bernardusz.healthcare_billing.user.User
import io.github.bernardusz.healthcare_billing.user.UserRole
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant

data class UserSecurity(
    val user: User
) : UserDetails {

    val id: Long
        get() = user.id ?: throw IllegalStateException("User ID cannot be null for security principal")

    val email: String
        get() = user.email

    val role: UserRole
        get() = user.role
    val createdAt: Instant
        get() = user.createdAt

    override fun getUsername(): String = user.username

    override fun getPassword(): String = user.password

    override fun getAuthorities(): Collection<GrantedAuthority> {
        return listOf(SimpleGrantedAuthority(user.role.name))
    }

    override fun isEnabled(): Boolean = true
    override fun isCredentialsNonExpired(): Boolean = true
    override fun isAccountNonExpired(): Boolean = true
    override fun isAccountNonLocked(): Boolean = true
}