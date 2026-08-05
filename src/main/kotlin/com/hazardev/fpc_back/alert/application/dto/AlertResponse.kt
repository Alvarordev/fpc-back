package com.hazardev.fpc_back.alert.application.dto

import com.hazardev.fpc_back.shared.domain.AlertSeverity
import com.hazardev.fpc_back.shared.domain.AlertStatus
import java.time.LocalDateTime
import java.util.UUID

data class AlertResponse(
    val id: UUID,
    val ticketNumber: String? = null,
    val healthCenterId: UUID,
    val healthCenterName: String,
    val contactId: UUID,
    val patientId: UUID? = null,
    val patientFullName: String? = null,
    val patientDni: String? = null,
    val patientPhone: String? = null,
    val createdByAgentId: UUID,
    val createdByAgentName: String,
    val title: String,
    val description: String,
    val status: AlertStatus,
    val severity: AlertSeverity = AlertSeverity.HIGH,
    val category: String = "GENERAL",
    val underReview: Boolean,
    val derivedTo: String?,
    val derivationNotes: String?,
    val aiSummary: String?,
    val resolvedAt: LocalDateTime?,
    val resolvedByAgentId: UUID?,
    val resolvedByAgentName: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
