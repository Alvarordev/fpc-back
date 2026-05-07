package com.hazardev.fpc_back.shared.security

import com.hazardev.fpc_back.shared.config.JwtProperties
import com.hazardev.fpc_back.user.domain.UserRole
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.Base64
import java.util.UUID

class JwtTokenProviderTest {

    private val validSecretBase64 = Base64.getEncoder().encodeToString(ByteArray(32) { 1 })

    private val jwtProperties = JwtProperties(
        secretBase64 = validSecretBase64,
        accessExpiration = 1000,
        refreshExpiration = 2000
    )
    private val jwtTokenProvider = JwtTokenProvider(jwtProperties)

    @Test
    fun `should generate valid access token`() {
        val userId = UUID.randomUUID()
        val token = jwtTokenProvider.generateAccessToken(userId, "test@test.com", UserRole.ADMIN)

        assertThat(jwtTokenProvider.validateToken(token)).isTrue()
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId)
    }

    @Test
    fun `should generate valid refresh token`() {
        val userId = UUID.randomUUID()
        val token = jwtTokenProvider.generateRefreshToken(userId)

        assertThat(jwtTokenProvider.validateToken(token)).isTrue()
        assertThat(jwtTokenProvider.getUserId(token)).isEqualTo(userId)
    }

    @Test
    fun `should extract authentication from token`() {
        val userId = UUID.randomUUID()
        val token = jwtTokenProvider.generateAccessToken(userId, "test@test.com", UserRole.ADMIN)

        val authentication = jwtTokenProvider.getAuthentication(token)

        assertThat(authentication.principal).isEqualTo(userId)
        assertThat(authentication.authorities.map { it.authority }).containsExactly("ROLE_ADMIN")
    }

    @Test
    fun `should reject invalid token`() {
        assertThat(jwtTokenProvider.validateToken("invalid-token")).isFalse()
    }

    @Test
    fun `should reject malformed token`() {
        assertThat(jwtTokenProvider.validateToken("Bearer invalid")).isFalse()
    }

    @Test
    fun `should reject expired token`() {
        val shortLivedProperties = JwtProperties(
            secretBase64 = validSecretBase64,
            accessExpiration = 1,
            refreshExpiration = 1
        )
        val shortLivedProvider = JwtTokenProvider(shortLivedProperties)
        val token = shortLivedProvider.generateAccessToken(UUID.randomUUID(), "test@test.com", UserRole.ADMIN)

        Thread.sleep(10)

        assertThat(shortLivedProvider.validateToken(token)).isFalse()
    }

    @Test
    fun `should fail fast when jwt secret is too short`() {
        val shortSecretBase64 = Base64.getEncoder().encodeToString(ByteArray(16) { 1 })

        val exception = assertThrows<IllegalStateException> {
            JwtProperties(
                secretBase64 = shortSecretBase64,
                accessExpiration = 1000,
                refreshExpiration = 2000
            )
        }

        assertThat(exception.message).contains("requires at least 256 bits")
    }

    @Test
    fun `should fail fast when jwt secret base64 is invalid`() {
        val exception = assertThrows<IllegalStateException> {
            JwtProperties(
                secretBase64 = "not-valid-base64***",
                accessExpiration = 1000,
                refreshExpiration = 2000
            )
        }

        assertThat(exception.message).contains("not valid Base64")
    }
}
