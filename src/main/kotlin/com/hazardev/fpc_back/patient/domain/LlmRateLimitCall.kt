package com.hazardev.fpc_back.patient.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "llm_rate_limit_calls")
class LlmRateLimitCall(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false, length = 20)
    val provider: String,

    @CreationTimestamp
    @Column(name = "reserved_at", nullable = false, updatable = false)
    val reservedAt: LocalDateTime? = null
)
