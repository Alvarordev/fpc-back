package com.hazardev.fpc_back.user.application

import com.hazardev.fpc_back.shared.security.JwtTokenProvider
import com.hazardev.fpc_back.user.domain.User
import com.hazardev.fpc_back.user.domain.UserRole
import com.hazardev.fpc_back.user.infrastructure.UserRepository
import io.jsonwebtoken.Claims
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AuthServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @InjectMocks
    private lateinit var authService: AuthService

    @Test
    fun `login should return tokens for valid credentials`() {
        val user = User(
            id = UUID.randomUUID(),
            email = "admin@gmail.com",
            passwordHash = "hashed",
            role = UserRole.ADMIN
        )
        whenever(userRepository.findByEmail("admin@gmail.com")).thenReturn(user)
        whenever(passwordEncoder.matches("123456", "hashed")).thenReturn(true)
        whenever(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token")
        whenever(jwtTokenProvider.generateRefreshToken(any())).thenReturn("refresh-token")

        val result = authService.login("admin@gmail.com", "123456")

        assertThat(result.accessToken).isEqualTo("access-token")
        assertThat(result.refreshToken).isEqualTo("refresh-token")
        assertThat(result.tokenType).isEqualTo("Bearer")
    }

    @Test
    fun `login should throw for invalid password`() {
        val user = User(
            id = UUID.randomUUID(),
            email = "admin@gmail.com",
            passwordHash = "hashed",
            role = UserRole.ADMIN
        )
        whenever(userRepository.findByEmail("admin@gmail.com")).thenReturn(user)
        whenever(passwordEncoder.matches("wrong", "hashed")).thenReturn(false)

        assertThrows<BadCredentialsException> {
            authService.login("admin@gmail.com", "wrong")
        }
    }

    @Test
    fun `login should throw for nonexistent user`() {
        whenever(userRepository.findByEmail("unknown@gmail.com")).thenReturn(null)

        assertThrows<BadCredentialsException> {
            authService.login("unknown@gmail.com", "password")
        }
    }

    @Test
    fun `login should throw for inactive user`() {
        val user = User(
            id = UUID.randomUUID(),
            email = "inactive@gmail.com",
            passwordHash = "hashed",
            role = UserRole.AGENT,
            isActive = false
        )
        whenever(userRepository.findByEmail("inactive@gmail.com")).thenReturn(user)
        whenever(passwordEncoder.matches("password", "hashed")).thenReturn(true)

        assertThrows<BadCredentialsException> {
            authService.login("inactive@gmail.com", "password")
        }
    }

    @Test
    fun `refresh should return new tokens for valid refresh token`() {
        val userId = UUID.randomUUID()
        val user = User(
            id = userId,
            email = "test@gmail.com",
            passwordHash = "hashed",
            role = UserRole.ADMIN
        )
        val refreshToken = "valid-refresh-token"
        val claims = org.mockito.kotlin.mock<Claims>()

        whenever(jwtTokenProvider.validateToken(refreshToken)).thenReturn(true)
        whenever(jwtTokenProvider.getClaims(refreshToken)).thenReturn(claims)
        whenever(claims.subject).thenReturn(userId.toString())
        whenever(claims["type"]).thenReturn("refresh")
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("new-access-token")
        whenever(jwtTokenProvider.generateRefreshToken(any())).thenReturn("new-refresh-token")

        val result = authService.refresh(refreshToken)

        assertThat(result.accessToken).isEqualTo("new-access-token")
        assertThat(result.refreshToken).isEqualTo("new-refresh-token")
    }

    @Test
    fun `refresh should throw for invalid token`() {
        whenever(jwtTokenProvider.validateToken("invalid")).thenReturn(false)

        assertThrows<BadCredentialsException> {
            authService.refresh("invalid")
        }
    }

    @Test
    fun `refresh should throw for access token`() {
        val token = "access-token"
        val claims = org.mockito.kotlin.mock<Claims>()

        whenever(jwtTokenProvider.validateToken(token)).thenReturn(true)
        whenever(jwtTokenProvider.getClaims(token)).thenReturn(claims)
        whenever(claims.subject).thenReturn(UUID.randomUUID().toString())
        whenever(claims["type"]).thenReturn("access")

        assertThrows<BadCredentialsException> {
            authService.refresh(token)
        }
    }
}
