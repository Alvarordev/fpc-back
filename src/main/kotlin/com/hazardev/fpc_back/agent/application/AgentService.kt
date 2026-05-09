package com.hazardev.fpc_back.agent.application

import com.hazardev.fpc_back.agent.application.dto.AgentResponse
import com.hazardev.fpc_back.agent.application.dto.CreateAgentRequest
import com.hazardev.fpc_back.agent.application.dto.UpdateAgentRequest
import com.hazardev.fpc_back.agent.domain.Agent
import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
import com.hazardev.fpc_back.user.domain.User
import com.hazardev.fpc_back.user.infrastructure.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class AgentService(
    private val agentRepository: AgentRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun createAgent(request: CreateAgentRequest): AgentResponse {
        if (agentRepository.existsByUserId(request.userId)) {
            throw IllegalStateException("User is already linked to an agent")
        }

        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("User not found") }

        val agent = Agent(
            user = user,
            fullName = request.fullName,
            phone = request.phone
        )

        return agentRepository.save(agent).toResponse()
    }

    fun getAgentById(id: UUID): AgentResponse {
        val agent = agentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Agent not found") }
        return agent.toResponse()
    }

    fun listAgents(): List<AgentResponse> {
        return agentRepository.findAll().map { it.toResponse() }
    }

    @Transactional
    fun updateAgent(id: UUID, request: UpdateAgentRequest): AgentResponse {
        val agent = agentRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Agent not found") }

        request.userId?.let { newUserId ->
            if (newUserId != agent.user.id && agentRepository.existsByUserId(newUserId)) {
                throw IllegalStateException("User is already linked to an agent")
            }

            val user = userRepository.findById(newUserId)
                .orElseThrow { EntityNotFoundException("User not found") }
            agent.user = user
        }

        request.fullName?.let { agent.fullName = it }
        request.phone?.let { agent.phone = it }

        return agentRepository.save(agent).toResponse()
    }

    @Transactional
    fun deleteAgent(id: UUID) {
        if (!agentRepository.existsById(id)) {
            throw EntityNotFoundException("Agent not found")
        }
        agentRepository.deleteById(id)
    }

    private fun Agent.toResponse(): AgentResponse = AgentResponse(
        id = id!!,
        userId = user.id!!,
        fullName = fullName,
        phone = phone,
        createdAt = createdAt!!
    )
}
