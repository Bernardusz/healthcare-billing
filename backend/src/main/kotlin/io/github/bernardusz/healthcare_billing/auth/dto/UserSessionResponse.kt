package io.github.bernardusz.healthcare_billing.auth.dto

import io.github.bernardusz.healthcare_billing.user.dto.UserInformationResponse

data class UserSessionResponse(
    val isAuthenticated: Boolean,
    val user: UserInformationResponse? = null
)
