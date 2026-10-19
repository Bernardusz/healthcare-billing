package io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.missing

class UserNotFoundException(
    message: String,
    cause: Throwable? = null,
    code: Int = 404
) : NotFoundException(message, cause, code)