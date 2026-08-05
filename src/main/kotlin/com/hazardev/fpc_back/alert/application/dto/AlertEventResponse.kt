package com.hazardev.fpc_back.alert.application.dto

import com.hazardev.fpc_back.alert.domain.AlertEventType
import java.time.LocalDateTime
import java.util.UUID

data class AlertEventResponse(
    val id: UUID,
    val alertId: UUID,
    val agentId: UUID?,
    val agentName: String?,
    val eventType: AlertEventType,
    val title: String,
    val description: String?,
    val createdAt: LocalDateTime
)
