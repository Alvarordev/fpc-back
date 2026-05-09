package com.hazardev.fpc_back.healthcenter.application.dto

import com.hazardev.fpc_back.shared.domain.PeruDepartment
import java.time.LocalDateTime

import java.util.UUID

data class HealthCenterResponse(
    val id: UUID,
    val name: String,
    val slug: String,
    val department: PeruDepartment,
    val isActive: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
