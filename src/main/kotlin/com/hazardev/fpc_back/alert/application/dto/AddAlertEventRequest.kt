package com.hazardev.fpc_back.alert.application.dto

import java.util.UUID

data class AddAlertEventRequest(
    val agentId: UUID? = null,
    val title: String,
    val description: String? = null
)
