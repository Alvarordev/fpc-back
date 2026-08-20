package com.hazardev.fpc_back.patient.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime

@Entity
@Table(name = "llm_rate_limit_guards")
class LlmRateLimitGuard(
    @Id
    @Column(nullable = false, length = 20)
    val provider: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
