package io.github.bernardusz.healthcare_billing.user

import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.external.InvalidPasswordException
import io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing.UserNotFoundException
import io.github.bernardusz.healthcare_billing.shared.utils.encodeNonNull
import io.github.bernardusz.healthcare_billing.user.dto.UserCreateRequest
import io.github.bernardusz.healthcare_billing.user.dto.UserInformationResponse
import io.github.bernardusz.healthcare_billing.user.dto.UserUpdateInformationRequest
import io.github.bernardusz.healthcare_billing.user.dto.UserUpdatePasswordRequest
import io.github.bernardusz.healthcare_billing.user.dto.UserUpdateRoleRequest
import io.github.bernardusz.healthcare_billing.user.dto.toInformationResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {
    @Transactional
    suspend fun createUser(
        userCreateRequest: UserCreateRequest
    ): Long {
        val hashedPassword: String = passwordEncoder.encodeNonNull(
            userCreateRequest.password
        )

        val newUser = User(
            username = userCreateRequest.username,
            email = userCreateRequest.email,
            password = hashedPassword,
            role = userCreateRequest.role,
        )

        val savedUser = userRepository.save(newUser)

        return savedUser.id
            ?: throw IllegalStateException("Database failed to generate user ID")
    }

    @Transactional(readOnly = true)
    fun getAllUsers(): Flow<UserInformationResponse> {
        val users: Flow<User> = userRepository.findAll()

        return users.map {
            user -> user.toInformationResponse()
        }
    }

    @Transactional(readOnly = true)
    fun searchAllUsersByUsernameAndEmail(
        searchTerm: String
    ): Flow<UserInformationResponse> {
        val users: Flow<User> = userRepository.searchByIdentifier(
            searchTerm = searchTerm,
        )

        return users.map {
            user -> user.toInformationResponse()
        }
    }

    @Transactional(readOnly = true)
    suspend fun getUserById(id: Long): UserInformationResponse {
        val user = userRepository.findById(id)
            ?: throw UserNotFoundException(
                "User with ID '$id' not found."
            )

        return user.toInformationResponse()
    }


    @Transactional
    suspend fun updateUserInformation(
        id: Long,
        request: UserUpdateInformationRequest
    ): UserInformationResponse {
        val existingUser: User = userRepository.findById(id)
        ?: throw UserNotFoundException(
            "User with ID '$id' not found."
        )

        val updatedUser = existingUser.copy(
            username = request.username,
            email = request.email,
        )

        return userRepository.save(updatedUser)
            .toInformationResponse()
    }

    @Transactional
    suspend fun updateUserRole(
        id: Long,
        userUpdateRoleRequest: UserUpdateRoleRequest
    ): UserInformationResponse {
        val user: User = userRepository.findById(id)
        ?: throw UserNotFoundException(
            "User with ID '$id' not found."
        )

        val updatedUser = user.copy(
            role = userUpdateRoleRequest.role,
        )

        return userRepository.save(updatedUser)
            .toInformationResponse()
    }

    @Transactional
    suspend fun updateUserPassword(
        id: Long,
        userUpdatePasswordRequest: UserUpdatePasswordRequest
    ) {
        val user: User = userRepository.findById(id)
        ?: throw UserNotFoundException(
            "User with ID '$id' not found."
        )

        if (!passwordEncoder.matches(
                userUpdatePasswordRequest.currentPassword,
                user.password
        )) {
            throw InvalidPasswordException(
                "Old Passwords do not match."
            )
        }

        if (passwordEncoder.matches(
            userUpdatePasswordRequest.newPassword,
            user.password
        ))
            throw InvalidPasswordException(
                "New Passwords cannot be the same as the current Password."
            )

        val hashedPassword: String = passwordEncoder.encodeNonNull(
            userUpdatePasswordRequest.newPassword
        )

        val updatedUser = user.copy(
            password = hashedPassword
        )

        userRepository.save(updatedUser)
    }

    @Transactional
    suspend fun deleteUser(id: Long) {
        userRepository.deleteById(id)
    }
}