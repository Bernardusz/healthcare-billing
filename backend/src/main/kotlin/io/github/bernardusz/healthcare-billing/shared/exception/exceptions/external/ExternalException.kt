package io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.external

open class ExternalException(
    message: String,
    cause: Throwable? = null,
    val code: Int
) : RuntimeException(message, cause)