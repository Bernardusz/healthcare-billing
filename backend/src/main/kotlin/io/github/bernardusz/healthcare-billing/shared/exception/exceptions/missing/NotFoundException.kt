package io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.missing

open class NotFoundException(
    message: String,
    cause: Throwable? = null,
    val code: Int
) : RuntimeException(message, cause)