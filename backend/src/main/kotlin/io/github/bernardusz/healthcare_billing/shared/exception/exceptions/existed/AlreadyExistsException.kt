package io.github.bernardusz.healthcare_billing.shared.exception.exceptions.existed

open class AlreadyExistsException(
    message: String,
    cause: Throwable? = null,
    val code: Int = 409
) : RuntimeException(message, cause)