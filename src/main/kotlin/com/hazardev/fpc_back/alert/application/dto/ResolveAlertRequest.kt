package com.hazardev.fpc_back.alert.application.dto

import java.util.UUID

data class ResolveAlertRequest(
    val resolvedByAgentId: UUID
)
