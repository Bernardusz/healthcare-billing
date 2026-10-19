package io.github.bernardusz.healthcare_billing.shared.exception.exceptions.external

class InvalidPasswordException(
    message: String,
    cause: Throwable? = null,
    code: Int = 401
) : ExternalException(message, cause, code)