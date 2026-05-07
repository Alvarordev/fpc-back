package com.hazardev.fpc_back.user.application

import com.hazardev.fpc_back.shared.security.JwtTokenProvider
import com.hazardev.fpc_back.user.application.dto.RefreshRequest
import com.hazardev.fpc_back.user.application.dto.TokenResponse
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

    fun login(email: String, password: String): TokenResponse {
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

        return TokenResponse(accessToken = accessToken, refreshToken = refreshToken)
    }

    fun refresh(refreshToken: String): TokenResponse {
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

        return TokenResponse(accessToken = accessToken, refreshToken = newRefreshToken)
    }
}
