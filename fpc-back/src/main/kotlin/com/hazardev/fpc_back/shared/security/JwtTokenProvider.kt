package com.hazardev.fpc_back.shared.security

import com.hazardev.fpc_back.shared.config.JwtProperties
import com.hazardev.fpc_back.user.domain.UserRole
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.stereotype.Component
import java.util.Date
import java.util.UUID

@Component
class JwtTokenProvider(
    private val jwtProperties: JwtProperties
) {
    private val secretKey = jwtProperties.signingKey()

    fun generateAccessToken(userId: UUID, email: String, role: UserRole): String {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("email", email)
            .claim("role", role.name)
            .claim("type", "access")
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.accessExpiration))
            .signWith(secretKey)
            .compact()
    }

    fun generateRefreshToken(userId: UUID): String {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("type", "refresh")
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.refreshExpiration))
            .signWith(secretKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            parseClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserId(token: String): UUID {
        return UUID.fromString(parseClaims(token).subject)
    }

    fun getAuthentication(token: String): Authentication {
        val claims = parseClaims(token)
        val userId = UUID.fromString(claims.subject)
        val role = claims["role"] as? String ?: "USER"
        val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
        return UsernamePasswordAuthenticationToken(userId, null, authorities)
    }

    fun getClaims(token: String): Claims {
        return parseClaims(token)
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(secretKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}
