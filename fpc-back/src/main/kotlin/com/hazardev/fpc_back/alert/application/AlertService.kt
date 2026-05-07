package com.hazardev.fpc_back.alert.application

import com.hazardev.fpc_back.agent.domain.Agent
import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
import com.hazardev.fpc_back.alert.application.dto.AlertResponse
import com.hazardev.fpc_back.alert.application.dto.CreateAlertRequest
import com.hazardev.fpc_back.alert.application.dto.ResolveAlertRequest
import com.hazardev.fpc_back.alert.domain.Alert
import com.hazardev.fpc_back.alert.infrastructure.AlertRepository
import com.hazardev.fpc_back.contact.infrastructure.ContactRepository
import com.hazardev.fpc_back.healthcenter.infrastructure.HealthCenterRepository
import com.hazardev.fpc_back.shared.domain.AlertStatus
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

/**
 * Manages alert entities with status lifecycle and validation rules.
 */
@Service
class AlertService(
    private val alertRepository: AlertRepository,
    private val healthCenterRepository: HealthCenterRepository,
    private val contactRepository: ContactRepository,
    private val agentRepository: AgentRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AlertService::class.java)
    }

    /**
     * Create a new alert with status ACTIVE.
     *
     * Validation rules:
     * - Health center must exist and be active
     * - Contact must exist
     * - Created-by agent must exist
     *
     * @param request containing healthCenterId, contactId, createdByAgentId, and description
     * @return the created alert as a response DTO
     * @throws EntityNotFoundException if health center, contact, or agent does not exist
     * @throws IllegalStateException if the health center is not active
     */
    @Transactional
    fun createAlert(request: CreateAlertRequest): AlertResponse {
        val healthCenter = healthCenterRepository.findById(request.healthCenterId)
            .orElseThrow {
                EntityNotFoundException("Health center not found with id: ${request.healthCenterId}")
            }

        if (!healthCenter.isActive) {
            throw IllegalStateException(
                "Cannot create alert for inactive health center: ${healthCenter.name} (id: ${healthCenter.id})"
            )
        }

        val contact = contactRepository.findById(request.contactId)
            .orElseThrow {
                EntityNotFoundException("Contact not found with id: ${request.contactId}")
            }

        val agent = agentRepository.findById(request.createdByAgentId)
            .orElseThrow {
                EntityNotFoundException("Agent not found with id: ${request.createdByAgentId}")
            }

        val alert = Alert(
            healthCenter = healthCenter,
            contact = contact,
            createdBy = agent,
            description = request.description,
            status = AlertStatus.ACTIVE
        )

        val saved = alertRepository.save(alert)
        logger.info(
            "Created alert: id={}, healthCenterId={}, agentId={}",
            saved.id, healthCenter.id, agent.id
        )
        return saved.toResponse()
    }

    /**
     * Resolve an alert by setting its status to RESOLVED.
     *
     * Business rules:
     * - Alert must be in ACTIVE status to be resolved
     * - Resolved-by agent must exist
     * - Sets resolvedAt to current timestamp
     *
     * @param alertId the ID of the alert to resolve
     * @param request containing the resolvedByAgentId
     * @return the resolved alert as a response DTO
     * @throws EntityNotFoundException if the alert or agent does not exist
     * @throws IllegalStateException if the alert is not in ACTIVE status
     */
    @Transactional
    fun resolveAlert(alertId: Long, request: ResolveAlertRequest): AlertResponse {
        val alert = alertRepository.findById(alertId)
            .orElseThrow {
                EntityNotFoundException("Alert not found with id: $alertId")
            }

        if (alert.status != AlertStatus.ACTIVE) {
            throw IllegalStateException(
                "Cannot resolve alert $alertId: current status is ${alert.status}, expected ACTIVE"
            )
        }

        val agent = agentRepository.findById(request.resolvedByAgentId)
            .orElseThrow {
                EntityNotFoundException("Agent not found with id: ${request.resolvedByAgentId}")
            }

        alert.status = AlertStatus.RESOLVED
        alert.resolvedAt = LocalDateTime.now()
        alert.resolvedBy = agent

        val saved = alertRepository.save(alert)
        logger.info(
            "Resolved alert: id={}, resolvedByAgentId={}",
            saved.id, agent.id
        )
        return saved.toResponse()
    }

    /**
     * Get all alerts associated with a specific health center.
     *
     * @param healthCenterId the health center's ID
     * @return list of alert response DTOs
     */
    fun getAlertsByHealthCenter(healthCenterId: Long): List<AlertResponse> {
        return alertRepository.findByHealthCenterId(healthCenterId)
            .map { it.toResponse() }
    }

    /**
     * Get all alerts that are currently in ACTIVE status.
     *
     * @return list of active alert response DTOs
     */
    fun getActiveAlerts(): List<AlertResponse> {
        return alertRepository.findByStatus(AlertStatus.ACTIVE)
            .map { it.toResponse() }
    }

    /**
     * Get all alerts created by a specific agent.
     *
     * @param agentId the agent's UUID
     * @return list of alert response DTOs created by that agent
     */
    fun getAlertsByAgent(agentId: UUID): List<AlertResponse> {
        return alertRepository.findByCreatedById(agentId)
            .map { it.toResponse() }
    }

    /**
     * Map entity to response DTO.
     */
    private fun Alert.toResponse(): AlertResponse = AlertResponse(
        id = id!!,
        healthCenterId = healthCenter.id!!,
        healthCenterName = healthCenter.name,
        contactId = contact.id!!,
        createdByAgentId = createdBy.id!!,
        createdByAgentName = createdBy.fullName,
        description = description,
        status = status,
        resolvedAt = resolvedAt,
        resolvedByAgentId = resolvedBy?.id,
        resolvedByAgentName = resolvedBy?.fullName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
