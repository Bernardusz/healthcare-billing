package io.github.bernardusz.healthcare_billing.shared.utils

import io.github.bernardusz.healthcare_billing.user.User
import io.github.bernardusz.healthcare_billing.user.UserRepository
import io.github.bernardusz.healthcare_billing.user.UserRole
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class DatabaseInitializer(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.bootstrap.admin.username:admin}") private val adminUsername: String,
    @Value("\${app.bootstrap.admin.email:admin@healthcare.local}") private val adminEmail: String,
    @Value("\${app.bootstrap.admin.password:AdminSecret123!}") private val adminPassword: String
) : ApplicationRunner {
    private val logger = LoggerFactory.getLogger(javaClass)
    override fun run(args: ApplicationArguments) {
        runBlocking {
            if (userRepository.count() == 0L) {
                logger.info("Initializing system: No users found. Creating initial system ADMIN...")
                val admin = User(
                    username = adminUsername,
                    email = adminEmail,
                    password = passwordEncoder.encodeNonNull(
                        adminPassword
                    ),
                    role = UserRole.ROLE_ADMIN,
                )
                userRepository.save(admin)
                logger.info("Initial ADMIN created successfully with username: {}", adminUsername)
            }
            else {
                logger.info("Database already contains initialized users. Skipping initialization...")
            }
        }
    }
}