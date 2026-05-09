package com.hazardev.fpc_back.alert.application.dto

import java.util.UUID

data class UpdateAlertRequest(
    val title: String? = null,
    val description: String? = null,
    val healthCenterId: UUID? = null,
    val contactId: UUID? = null
)
