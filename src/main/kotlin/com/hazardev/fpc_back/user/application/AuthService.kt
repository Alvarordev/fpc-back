package com.hazardev.fpc_back.user.application

import com.hazardev.fpc_back.shared.security.JwtTokenProvider
import com.hazardev.fpc_back.user.application.dto.AuthResponse
import com.hazardev.fpc_back.user.application.dto.RefreshRequest
import com.hazardev.fpc_back.user.application.dto.UserResponse
import com.hazardev.fpc_back.user.infrastructure.UserRepository
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun login(email: String, password: String): AuthResponse {
        val user = userRepository.findByEmail(email)
            ?: throw BadCredentialsException("Invalid credentials")

        if (!passwordEncoder.matches(password, user.passwordHash)) {
            throw BadCredentialsException("Invalid credentials")
        }

        if (!user.isActive) {
            throw BadCredentialsException("User is inactive")
        }

        val userId = user.id ?: throw BadCredentialsException("Invalid user")
        val accessToken = jwtTokenProvider.generateAccessToken(userId, user.email, user.role)
        val refreshToken = jwtTokenProvider.generateRefreshToken(userId)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = refreshToken,
            user = UserResponse(
                id = userId,
                email = user.email,
                role = user.role,
                isActive = user.isActive,
                createdAt = user.createdAt!!,
                updatedAt = user.updatedAt!!
            )
        )
    }

    fun refresh(refreshToken: String): AuthResponse {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw BadCredentialsException("Invalid refresh token")
        }

        val claims = jwtTokenProvider.getClaims(refreshToken)
        if (claims["type"] != "refresh") {
            throw BadCredentialsException("Invalid token type")
        }

        val userId = UUID.fromString(claims.subject)
        val user = userRepository.findById(userId)
            .orElseThrow { UsernameNotFoundException("User not found") }

        val refreshedUserId = user.id ?: throw BadCredentialsException("Invalid user")
        val accessToken = jwtTokenProvider.generateAccessToken(refreshedUserId, user.email, user.role)
        val newRefreshToken = jwtTokenProvider.generateRefreshToken(refreshedUserId)

        return AuthResponse(
            accessToken = accessToken,
            refreshToken = newRefreshToken,
            user = UserResponse(
                id = refreshedUserId,
                email = user.email,
                role = user.role,
                isActive = user.isActive,
                createdAt = user.createdAt!!,
                updatedAt = user.updatedAt!!
            )
        )
    }
}
