package com.hazardev.fpc_back.alert.application.dto

import java.util.UUID

data class CreateAlertRequest(
    val healthCenterId: UUID,
    val contactId: UUID? = null,
    val patientId: UUID? = null,
    val createdByAgentId: UUID? = null,
    val title: String,
    val description: String
)
