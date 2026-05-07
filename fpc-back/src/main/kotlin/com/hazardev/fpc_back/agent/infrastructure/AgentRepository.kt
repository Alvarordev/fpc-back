package com.hazardev.fpc_back.agent.infrastructure

import com.hazardev.fpc_back.agent.domain.Agent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AgentRepository : JpaRepository<Agent, UUID> {
    fun existsByUserId(userId: UUID): Boolean
}
