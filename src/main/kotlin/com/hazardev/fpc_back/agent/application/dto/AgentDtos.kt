package com.hazardev.fpc_back.agent.application.dto

import java.time.Instant
import java.util.UUID

data class CreateAgentRequest(
    val userId: UUID,
    val fullName: String,
    val phone: String
)

data class UpdateAgentRequest(
    val userId: UUID? = null,
    val fullName: String? = null,
    val phone: String? = null
)

data class AgentResponse(
    val id: UUID,
    val userId: UUID,
    val fullName: String,
    val phone: String,
    val createdAt: Instant
)
