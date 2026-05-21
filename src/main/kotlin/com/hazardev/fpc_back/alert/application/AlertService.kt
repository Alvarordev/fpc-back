package com.hazardev.fpc_back.alert.application

import com.hazardev.fpc_back.agent.domain.Agent
import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
import com.hazardev.fpc_back.alert.application.dto.AlertResponse
import com.hazardev.fpc_back.alert.application.dto.CreateAlertRequest
import com.hazardev.fpc_back.alert.application.dto.ResolveAlertRequest
import com.hazardev.fpc_back.alert.application.dto.UpdateAlertRequest
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
            title = request.title,
            description = request.description,
            status = AlertStatus.ACTIVE
        )

        val saved = alertRepository.saveAndFlush(alert)
        logger.info(
            "Created alert: id={}, healthCenterId={}, agentId={}",
            saved.id, healthCenter.id, agent.id
        )
        return saved.toResponse()
    }

    @Transactional
    fun resolveAlert(alertId: UUID, request: ResolveAlertRequest): AlertResponse {
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

        val saved = alertRepository.saveAndFlush(alert)
        logger.info(
            "Resolved alert: id={}, resolvedByAgentId={}",
            saved.id, agent.id
        )
        return saved.toResponse()
    }

    fun getAlertsByHealthCenter(healthCenterId: UUID): List<AlertResponse> {
        return alertRepository.findByHealthCenterId(healthCenterId)
            .map { it.toResponse() }
    }

    fun getActiveAlerts(): List<AlertResponse> {
        return alertRepository.findByStatus(AlertStatus.ACTIVE)
            .map { it.toResponse() }
    }

    fun getAlertsByAgent(agentId: UUID): List<AlertResponse> {
        return alertRepository.findByCreatedById(agentId)
            .map { it.toResponse() }
    }

    private fun Alert.toResponse(): AlertResponse = AlertResponse(
        id = id ?: throw IllegalStateException("Alert ID is null after save"),
        healthCenterId = healthCenter.id ?: throw IllegalStateException("HealthCenter ID is null on alert"),
        healthCenterName = healthCenter.name,
        contactId = contact.id ?: throw IllegalStateException("Contact ID is null on alert"),
        createdByAgentId = createdBy.id ?: throw IllegalStateException("CreatedBy agent ID is null on alert"),
        createdByAgentName = createdBy.fullName,
        title = title,
        description = description,
        status = status,
        resolvedAt = resolvedAt,
        resolvedByAgentId = resolvedBy?.id,
        resolvedByAgentName = resolvedBy?.fullName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    fun getAlertById(id: UUID): AlertResponse {
        val alert = alertRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $id") }
        return alert.toResponse()
    }

    fun getAllAlerts(): List<AlertResponse> {
        return alertRepository.findAll().map { it.toResponse() }
    }

    @Transactional
    fun updateAlert(id: UUID, request: UpdateAlertRequest): AlertResponse {
        val alert = alertRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $id") }

        request.description?.let { alert.description = it }

        request.title?.let { alert.title = it }

        request.healthCenterId?.let { healthCenterId ->
            val healthCenter = healthCenterRepository.findById(healthCenterId)
                .orElseThrow { EntityNotFoundException("Health center not found with id: $healthCenterId") }
            alert.healthCenter = healthCenter
        }

        request.contactId?.let { contactId ->
            val contact = contactRepository.findById(contactId)
                .orElseThrow { EntityNotFoundException("Contact not found with id: $contactId") }
            alert.contact = contact
        }

        return alertRepository.saveAndFlush(alert).toResponse()
    }

    @Transactional
    fun deleteAlert(id: UUID) {
        val alert = alertRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $id") }
        alertRepository.delete(alert)
    }
}
