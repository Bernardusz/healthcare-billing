package io.github.bernardusz.`healthcare-billing`.user

import kotlinx.coroutines.flow.Flow
import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository: CoroutineCrudRepository<User, Long> {
    suspend fun findByEmail(email: String): User?
    suspend fun findByUsername(username: String): User?
    suspend fun existsByEmail(email: String): Boolean
    suspend fun existsByUsername(username: String): Boolean

    @Query("""
        SELECT * FROM users
            WHERE email = :identifier
            OR username = :identifier
    """)
    suspend fun findByIdentifierSecurity(identifier: String): User?

    fun findByRole(role: String): Flow<User>

    @Query("""
        SELECT
         username, email, role, createdAt
         FROM users 
        WHERE username ILIKE CONCAT('%', :searchTerm, '%') 
           OR email ILIKE CONCAT('%', :searchTerm, '%')
    """)
    fun searchByIdentifier(searchTerm: String): Flow<User>
}