package com.hazardev.fpc_back.alert.application.dto

import java.util.UUID

data class CreateAlertRequest(
    val healthCenterId: Long,
    val contactId: Long,
    val createdByAgentId: UUID,
    val description: String
)
