package io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.existed

class UserAlreadyExistsException(
    message: String,
    cause: Throwable? = null,
    code: Int = 409
): AlreadyExistsException(message, cause, code)