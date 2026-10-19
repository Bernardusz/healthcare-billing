package io.github.bernardusz.`healthcare-billing`.user.dto

import io.github.bernardusz.`healthcare-billing`.user.User
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import java.time.Instant

data class UserSecurity(
    val user: User,
): UserDetails {
    fun getId(): Long = user.id
        ?: throw IllegalStateException("User ID cannot be null for security principal")
    fun getEmail(): String = user.email
    override fun getUsername(): String {
        return user.username
    }
    override fun getPassword(): String {
        return user.password
    }
    fun getRole(): String = user.role
    fun getCreatedAt(): Instant = user.createdAt

    override fun getAuthorities(): Collection<GrantedAuthority> {
        // Automatically prefix with ROLE_ if missing
        val formattedRole = if (user.role.startsWith("ROLE_")) user.role else "ROLE_${user.role}"
        return listOf(SimpleGrantedAuthority(formattedRole))
    }

    override fun isEnabled(): Boolean {
        return true
    }
    override fun isCredentialsNonExpired(): Boolean {
        return true
    }
    override fun isAccountNonExpired(): Boolean {
        return true
    }
    override fun isAccountNonLocked(): Boolean {
        return true
    }
}
