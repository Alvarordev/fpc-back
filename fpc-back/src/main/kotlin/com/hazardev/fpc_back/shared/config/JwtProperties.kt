package com.hazardev.fpc_back.shared.config

import io.jsonwebtoken.security.Keys
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.Base64
import javax.crypto.SecretKey

@ConfigurationProperties(prefix = "jwt")
data class JwtProperties(
    val secretBase64: String = "",
    val accessExpiration: Long = 900000,
    val refreshExpiration: Long = 604800000
) {
    init {
        require(accessExpiration > 0) { "Invalid JWT config: 'jwt.access-expiration' must be greater than 0." }
        require(refreshExpiration > 0) { "Invalid JWT config: 'jwt.refresh-expiration' must be greater than 0." }

        resolveSigningKeyBytes()
    }

    fun signingKey(): SecretKey = Keys.hmacShaKeyFor(resolveSigningKeyBytes())

    private fun resolveSigningKeyBytes(): ByteArray {
        if (secretBase64.isBlank()) {
            throw IllegalStateException(
                "Invalid JWT config: signing key is missing. Set 'jwt.secret-base64'."
            )
        }

        val keyBytes = decodeBase64Secret(secretBase64)

        val minBytes = 32
        if (keyBytes.size < minBytes) {
            val bits = keyBytes.size * 8
            throw IllegalStateException(
                "Invalid JWT config: signing key is $bits bits, but HS256 requires at least 256 bits (32 bytes). " +
                    "Use 'jwt.secret-base64' with a Base64-encoded 32+ byte key."
            )
        }

        return keyBytes
    }

    private fun decodeBase64Secret(base64Secret: String): ByteArray {
        return try {
            Base64.getDecoder().decode(base64Secret.trim())
        } catch (_: IllegalArgumentException) {
            throw IllegalStateException(
                "Invalid JWT config: 'jwt.secret-base64' is not valid Base64. " +
                    "Generate one with: openssl rand -base64 32"
            )
        }
    }
}
