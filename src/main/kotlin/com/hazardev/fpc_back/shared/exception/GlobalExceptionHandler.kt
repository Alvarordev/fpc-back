package com.hazardev.fpc_back.shared.exception

import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import java.time.Instant

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(BadCredentialsException::class, AuthenticationException::class)
    fun handleAuthentication(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(
                ErrorResponse(
                    timestamp = Instant.now(),
                    status = 401,
                    error = "Unauthorized",
                    message = ex.message ?: "Authentication failed",
                    path = extractPath(request)
                )
            )
    }

    @ExceptionHandler(AccessDeniedException::class)
    fun handleAccessDenied(ex: AccessDeniedException, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(
                ErrorResponse(
                    timestamp = Instant.now(),
                    status = 403,
                    error = "Forbidden",
                    message = ex.message ?: "Access denied",
                    path = extractPath(request)
                )
            )
    }

    @ExceptionHandler(EntityNotFoundException::class, UsernameNotFoundException::class)
    fun handleNotFound(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(
                ErrorResponse(
                    timestamp = Instant.now(),
                    status = 404,
                    error = "Not Found",
                    message = ex.message ?: "Resource not found",
                    path = extractPath(request)
                )
            )
    }

    @ExceptionHandler(IllegalArgumentException::class, IllegalStateException::class)
    fun handleBadRequest(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(
                ErrorResponse(
                    timestamp = Instant.now(),
                    status = 400,
                    error = "Bad Request",
                    message = ex.message ?: "Invalid request",
                    path = extractPath(request)
                )
            )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(ex: Exception, request: WebRequest): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                ErrorResponse(
                    timestamp = Instant.now(),
                    status = 500,
                    error = "Internal Server Error",
                    message = ex.message ?: "An unexpected error occurred",
                    path = extractPath(request)
                )
            )
    }

    private fun extractPath(request: WebRequest): String {
        return if (request is ServletWebRequest) {
            request.request.requestURI
        } else {
            ""
        }
    }
}

data class ErrorResponse(
    val timestamp: Instant,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
