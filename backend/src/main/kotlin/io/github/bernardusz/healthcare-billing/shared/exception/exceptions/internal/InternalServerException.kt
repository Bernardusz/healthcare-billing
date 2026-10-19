package io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.internal

class InternalServerException(
    message: String,
    cause: Throwable? = null,
    val code: Int = 500
) : RuntimeException(message, cause)