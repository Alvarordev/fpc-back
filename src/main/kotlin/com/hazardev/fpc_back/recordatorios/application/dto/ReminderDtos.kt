package com.hazardev.fpc_back.recordatorios.application.dto

import com.hazardev.fpc_back.shared.domain.ReminderStatus
import com.hazardev.fpc_back.shared.domain.ReminderType
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class CreateReminderRequest(
    val patientId: UUID,
    val contactId: UUID,
    val type: ReminderType,
    val description: String,
    val scheduledDate: LocalDate,
    val notes: String? = null
)

data class UpdateReminderRequest(
    val type: ReminderType? = null,
    val description: String? = null,
    val scheduledDate: LocalDate? = null,
    val notes: String? = null
)

data class ReminderResponse(
    val id: UUID,
    val patientId: UUID,
    val contactId: UUID,
    val type: ReminderType,
    val description: String,
    val scheduledDate: LocalDate,
    val status: ReminderStatus,
    val notes: String?,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)
