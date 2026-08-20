package com.hazardev.fpc_back.patient.application

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "patient.summary")
data class PatientSummaryProperties(
    val schedulerDelayMs: Long = 5000,
    val maxBatchSize: Int = 3,
    val processingTimeoutSeconds: Long = 300,
    val maxRetries: Int = 7,
    val rateLimitPerMinute: Int = 7
)
