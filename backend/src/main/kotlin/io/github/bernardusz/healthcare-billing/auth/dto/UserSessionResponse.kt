package io.github.bernardusz.`healthcare-billing`.auth.dto

import io.github.bernardusz.`healthcare-billing`.user.dto.UserInformationResponse

data class UserSessionResponse(
    val isAuthenticated: Boolean,
    val user: UserInformationResponse? = null
)
