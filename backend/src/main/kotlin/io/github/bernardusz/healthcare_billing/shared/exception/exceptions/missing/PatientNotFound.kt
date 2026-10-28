package io.github.bernardusz.healthcare_billing.shared.exception.exceptions.missing

class PatientNotFound (
    message: String,
    cause: Throwable? = null,
    code: Int = 404
) : NotFoundException(message, cause, code)