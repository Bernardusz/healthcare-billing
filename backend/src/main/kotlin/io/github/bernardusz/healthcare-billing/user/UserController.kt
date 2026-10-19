package io.github.bernardusz.`healthcare-billing`.user

import io.github.bernardusz.`healthcare-billing`.auth.AuthService
import io.github.bernardusz.`healthcare-billing`.shared.utils.createAccessTokenCookie
import io.github.bernardusz.`healthcare-billing`.shared.utils.createRefreshTokenCookie
import io.github.bernardusz.`healthcare-billing`.user.dto.UserCreateRequest
import io.github.bernardusz.`healthcare-billing`.user.dto.UserInformationResponse
import io.github.bernardusz.`healthcare-billing`.user.dto.UserSecurity
import io.github.bernardusz.`healthcare-billing`.user.dto.UserUpdateInformationRequest
import io.github.bernardusz.`healthcare-billing`.user.dto.UserUpdatePasswordRequest
import io.github.bernardusz.`healthcare-billing`.user.dto.UserUpdateRoleRequest
import jakarta.validation.Valid
import kotlinx.coroutines.flow.Flow
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val authService: AuthService
) {
    @PreAuthorize("hasRole('USER')")
    @PostMapping
    suspend fun createUser(
        @Valid @RequestBody userCreateRequest: UserCreateRequest
    ): ResponseEntity<Void> {
        val userId: Long = userService.createUser(
            userCreateRequest
        )
        return ResponseEntity.created(
            URI.create("/api/v1/users/$userId")
        ).build()
    }

    @GetMapping
    fun searchUsers(
        @RequestParam(required = false) searchString: String?
    ): ResponseEntity<Flow<UserInformationResponse>> {
        val results: Flow<UserInformationResponse> = if (searchString.isNullOrBlank()) {
            userService.getAllUsers()
        } else {
            userService.searchAllUsersByUsernameAndEmail(searchString)
        }

        return ResponseEntity.ok(results)
    }

    @GetMapping("/{userId}")
    suspend fun getUserById(
        @PathVariable userId: Long
    ): ResponseEntity<UserInformationResponse> {
        return ResponseEntity.ok(
            userService.getUserById(userId)
        )
    }

    @GetMapping("/username/{username}")
    suspend fun getUserByUsername(
        @PathVariable username: String
    ): ResponseEntity<UserInformationResponse> {
        return ResponseEntity.ok(
            userService.getUserByUsername(username)
        )
    }

    @GetMapping("/email/{email}")
    suspend fun getUserByEmail(
        @PathVariable email: String
    ): ResponseEntity<UserInformationResponse> {
        return ResponseEntity.ok(
            userService.getUserByEmail(
                email = email
            )
        )
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/information")
    suspend fun updateUserInformation(
        @AuthenticationPrincipal user: UserSecurity,
        @Valid @RequestBody userUpdateInformationRequest: UserUpdateInformationRequest
    ): ResponseEntity<UserInformationResponse> {
        val updatedUser: UserInformationResponse =
        userService.updateUserInformation(
            user.getId(),
            userUpdateInformationRequest
        )

        return ResponseEntity.ok(updatedUser)
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/role")
    suspend fun updateUserRole(
        @PathVariable id: Long,
        @Valid @RequestBody userUpdateRoleRequest: UserUpdateRoleRequest,
    ): ResponseEntity<UserInformationResponse> {
        val updatedUser: UserInformationResponse = userService
            .updateUserRole(
                id = id,
                userUpdateRoleRequest = userUpdateRoleRequest
            )

        return ResponseEntity.ok(updatedUser)
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/password")
    suspend fun updatePassword(
        @AuthenticationPrincipal user: UserSecurity,
        @Valid @RequestBody userUpdatePasswordRequest: UserUpdatePasswordRequest,
        response: ServerHttpResponse
    ): ResponseEntity<Void> {
        userService.updateUserPassword(
            id = user.getId(),
            userUpdatePasswordRequest = userUpdatePasswordRequest
        )

        val accessTokenCookie: ResponseCookie =
            createAccessTokenCookie("", 0)

        val refreshTokenCookie: ResponseCookie =
            createRefreshTokenCookie("", 0)

        response.addCookie(accessTokenCookie)
        response.addCookie(refreshTokenCookie)

        return ResponseEntity.ok().build()
    }

    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.id")
    @DeleteMapping("/{id}")
    suspend fun deleteUserById(
        @PathVariable id: Long,
        @AuthenticationPrincipal user: UserSecurity,
        response: ServerHttpResponse
    ): ResponseEntity<Void> {
        if (id == user.getId()){
            val accessToken: ResponseCookie =
                createAccessTokenCookie(
                    "",
                    0
                )

            val refreshToken: ResponseCookie =
                createRefreshTokenCookie(
                    "",
                    0
                )

            response.addCookie(accessToken)
            response.addCookie(refreshToken)

            return ResponseEntity.noContent().build()
        }
        return ResponseEntity.noContent().build()
    }
}