package io.github.bernardusz.`healthcare-billing`.auth.service

import io.github.bernardusz.`healthcare-billing`.user.User
import io.github.bernardusz.`healthcare-billing`.user.UserRepository
import io.github.bernardusz.`healthcare-billing`.user.dto.UserSecurity
import kotlinx.coroutines.reactor.mono
import org.springframework.security.core.userdetails.ReactiveUserDetailsService
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class CustomUserDetailsService(
    val userRepository: UserRepository
) : ReactiveUserDetailsService {
    override fun findByUsername(identifier: String): Mono<UserDetails> = mono {
        val user = userRepository.findByIdentifierSecurity(identifier) ?:
            throw UsernameNotFoundException("User not found with identifier: $identifier")

        UserSecurity(user)
    }

    suspend fun loadUserById(userId: String): UserSecurity {
        val user: User = (userRepository.findById(userId.toLong())
            ?: throw UsernameNotFoundException("User not found with identifier: $userId"))

        return UserSecurity(user)
    }
}