package io.github.bernardusz.`healthcare-billing`.shared.exception

import io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.external.ExternalException
import io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.internal.InternalServerException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import javax.naming.AuthenticationException
import io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.existed.AlreadyExistsException
import io.github.bernardusz.`healthcare-billing`.shared.exception.exceptions.missing.NotFoundException
import io.jsonwebtoken.ExpiredJwtException

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        ex: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val fieldError = ex.bindingResult.fieldErrors.firstOrNull()
        val message = fieldError?.let { "${it.field}: ${it.defaultMessage}" }
            ?: "Validation failed for request body."

        val errorResponse = ErrorResponse(
            message = message,
            code = HttpStatus.BAD_REQUEST.value(),
            timestamp = Instant.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // 2. Authentication Failures (Invalid credentials, bad tokens, etc.)
    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Authentication failed.",
            code = HttpStatus.UNAUTHORIZED.value(),
            timestamp = Instant.now()
        )
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse)
    }

    // 3. Authorization / Access Denied (User lacks required role/permissions)
    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDeniedException(
        ex: AccessDeniedException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = "You do not have permission to access this resource.",
            code = HttpStatus.FORBIDDEN.value(),
            timestamp = Instant.now()
        )
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse)
    }

    // 4. Custom Business Exceptions (e.g., UserAlreadyExists, ResourceNotFound)
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Invalid request argument.",
            code = HttpStatus.BAD_REQUEST.value(),
            timestamp = Instant.now()
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    // 5. Catch-All Fallback for Unexpected Internal Server Errors
    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        ex: Exception
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = "An unexpected error occurred. Please try again later.",
            code = HttpStatus.INTERNAL_SERVER_ERROR.value(),
            timestamp = Instant.now()
        )
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse)
    }

    @ExceptionHandler(InternalServerException::class)
    fun handleInternalServerException(
        ex: InternalServerException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "An unexpected error occurred. Please try again later.",
            code = ex.code,
            timestamp = Instant.now()
        )

        return ResponseEntity.status(ex.code).body(errorResponse)
    }


    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(ex: HttpMessageNotReadableException): ResponseEntity<Map<String, String>> {
        val errorResponse = mapOf(
            "error" to "BAD_REQUEST",
            "message" to "Malformed JSON request body or invalid data type"
        )
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(ExternalException::class)
    fun handleInvalidPasswordException(
        ex: ExternalException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Invalid password",
            code = ex.code,
            timestamp = Instant.now()
        )

        return ResponseEntity.status(ex.code).body(errorResponse)
    }

    @ExceptionHandler(AlreadyExistsException::class)
    fun handleAlreadyExistsException(
        ex: AlreadyExistsException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "User already exists",
            code = ex.code,
            timestamp = Instant.now()
        )

        return ResponseEntity.status(ex.code).body(errorResponse)
    }

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "User not found",
            code = ex.code,
            timestamp = Instant.now()
        )

        return ResponseEntity.status(ex.code).body(errorResponse)
    }

    @ExceptionHandler(ExpiredJwtException::class)
    fun handleExpiredJwtException(
        ex: ExpiredJwtException
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            message = ex.message ?: "Expired token",
            code = HttpStatus.UNAUTHORIZED.value(),
            timestamp = Instant.now()
        )

        return ResponseEntity.status(errorResponse.code).body(errorResponse)
    }
}