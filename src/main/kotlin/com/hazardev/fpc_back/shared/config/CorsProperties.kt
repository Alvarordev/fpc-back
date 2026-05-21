package com.hazardev.fpc_back.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app.cors")
data class CorsProperties(
    val allowedOriginPatterns: List<String> = emptyList(),
    val allowedMethods: List<String> = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
    val allowedHeaders: List<String> = listOf("Authorization", "Content-Type", "Accept", "Origin"),
    val exposedHeaders: List<String> = emptyList(),
    val allowCredentials: Boolean = false,
    val maxAgeSeconds: Long = 3600
)
