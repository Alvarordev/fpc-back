package com.hazardev.fpc_back.patient.application.dto

import com.hazardev.fpc_back.shared.domain.PatientStatus
import java.util.UUID

data class ChangeStatusRequest(
    val newStatus: PatientStatus
)

data class LinkCompanionRequest(
    val companionId: UUID,
    val isPrimaryInformant: Boolean = false
)
