package com.hazardev.fpc_back.patient.application.dto

import com.hazardev.fpc_back.shared.domain.PatientStatus
import java.time.LocalDateTime
import java.util.UUID

data class ChangeStatusRequest(
    val newStatus: PatientStatus
)

data class LinkCompanionRequest(
    val companionId: UUID,
    val isPrimaryInformant: Boolean = false
)

data class PatientSummaryOnDemandResponse(
    val patientId: UUID,
    val dni: String,
    val result: String,
    val summary: PatientSummaryResponse,
    val metadata: PatientSummaryOnDemandMetadata,
    val error: PatientSummaryOnDemandError? = null
)

data class PatientSummaryOnDemandMetadata(
    val generatedOnDemand: Boolean,
    val fallbackUsed: Boolean,
    val stale: Boolean,
    val summaryUpdatedAt: LocalDateTime?,
    val sourceUpdatedAt: LocalDateTime?,
    val generatedFromSourceUpdatedAt: LocalDateTime?,
    val retryCount: Int,
    val lastAttemptAt: LocalDateTime?,
    val nextAttemptAt: LocalDateTime?,
    val lastErrorCode: String? = null
)

data class PatientSummaryOnDemandError(
    val code: String,
    val message: String,
    val temporary: Boolean
)
