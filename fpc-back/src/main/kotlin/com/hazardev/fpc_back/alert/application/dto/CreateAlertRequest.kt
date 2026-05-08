package com.hazardev.fpc_back.alert.application.dto

import java.util.UUID

data class CreateAlertRequest(
    val healthCenterId: UUID,
    val contactId: UUID,
    val createdByAgentId: UUID,
    val description: String
)
