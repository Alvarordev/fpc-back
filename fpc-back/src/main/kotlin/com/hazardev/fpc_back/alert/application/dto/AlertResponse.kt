package com.hazardev.fpc_back.alert.application.dto

import com.hazardev.fpc_back.shared.domain.AlertStatus
import java.time.LocalDateTime
import java.util.UUID

data class AlertResponse(
    val id: UUID,
    val healthCenterId: UUID,
    val healthCenterName: String,
    val contactId: UUID,
    val createdByAgentId: UUID,
    val createdByAgentName: String,
    val description: String,
    val status: AlertStatus,
    val resolvedAt: LocalDateTime?,
    val resolvedByAgentId: UUID?,
    val resolvedByAgentName: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
