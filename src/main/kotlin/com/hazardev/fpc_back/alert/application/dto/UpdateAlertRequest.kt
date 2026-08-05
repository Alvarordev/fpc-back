package com.hazardev.fpc_back.alert.application.dto

import com.hazardev.fpc_back.shared.domain.AlertStatus
import java.util.UUID

data class UpdateAlertRequest(
    val title: String? = null,
    val description: String? = null,
    val healthCenterId: UUID? = null,
    val contactId: UUID? = null,
    val status: AlertStatus? = null,
    val underReview: Boolean? = null,
    val derivedTo: String? = null,
    val derivationNotes: String? = null
)
