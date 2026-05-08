package com.hazardev.fpc_back.user.application.dto

import com.hazardev.fpc_back.user.domain.UserRole
import java.time.Instant
import java.util.UUID

data class LoginRequest(
    val email: String,
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

data class CreateUserRequest(
    val email: String,
    val password: String,
    val role: UserRole
)

data class UpdateUserRequest(
    val email: String? = null,
    val password: String? = null,
    val role: UserRole? = null,
    val isActive: Boolean? = null
)

data class UserResponse(
    val id: UUID,
    val email: String,
    val role: UserRole,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant
)
