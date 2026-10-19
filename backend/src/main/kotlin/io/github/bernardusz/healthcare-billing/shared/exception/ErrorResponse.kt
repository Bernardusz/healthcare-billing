package io.github.bernardusz.`healthcare-billing`.shared.exception

import java.time.Instant

data class ErrorResponse(
    val message: String,
    val code: Int,
    val timestamp: Instant = Instant.now(),
)
