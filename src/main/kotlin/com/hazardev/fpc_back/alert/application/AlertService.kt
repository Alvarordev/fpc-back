package com.hazardev.fpc_back.alert.application

import com.hazardev.fpc_back.agent.domain.Agent
import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
import com.hazardev.fpc_back.alert.application.dto.AddAlertEventRequest
import com.hazardev.fpc_back.alert.application.dto.AlertEventResponse
import com.hazardev.fpc_back.alert.application.dto.AlertResponse
import com.hazardev.fpc_back.alert.application.dto.CreateAlertRequest
import com.hazardev.fpc_back.alert.application.dto.ResolveAlertRequest
import com.hazardev.fpc_back.alert.application.dto.UpdateAlertRequest
import com.hazardev.fpc_back.alert.domain.Alert
import com.hazardev.fpc_back.alert.domain.AlertEvent
import com.hazardev.fpc_back.alert.domain.AlertEventType
import com.hazardev.fpc_back.alert.infrastructure.AlertEventRepository
import com.hazardev.fpc_back.alert.infrastructure.AlertRepository
import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.contact.infrastructure.ContactRepository
import com.hazardev.fpc_back.healthcenter.infrastructure.HealthCenterRepository
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.shared.domain.AlertSeverity
import com.hazardev.fpc_back.shared.domain.AlertStatus
import com.hazardev.fpc_back.shared.domain.ContactPurpose
import com.hazardev.fpc_back.shared.domain.ContactStatus
import com.hazardev.fpc_back.shared.domain.ContactType
import com.hazardev.fpc_back.shared.service.N8nWebhookService
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class AlertService(
    private val alertRepository: AlertRepository,
    private val alertEventRepository: AlertEventRepository,
    private val healthCenterRepository: HealthCenterRepository,
    private val contactRepository: ContactRepository,
    private val patientRepository: PatientRepository,
    private val agentRepository: AgentRepository,
    private val aiService: AIService,
    private val n8nWebhookService: N8nWebhookService,
    private val jdbcTemplate: JdbcTemplate
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AlertService::class.java)
    }

    private fun findAgentByIdOrUserId(id: UUID): Agent {
        return agentRepository.findById(id)
            .orElseGet {
                agentRepository.findByUserId(id)
                    .orElseGet {
                        agentRepository.findAll().firstOrNull()
                            ?: throw EntityNotFoundException("No agent registered in system")
                    }
            }
    }

    private fun getOrCreateDefaultContact(): Contact {
        val existing = contactRepository.findAll().firstOrNull()
        if (existing != null) return existing

        val patient = patientRepository.findAll().firstOrNull() ?: patientRepository.save(
            Patient(
                fullName = "Sistema de Alertas",
                primaryPhone = "000000000"
            )
        )
        return contactRepository.save(
            Contact(
                patient = patient,
                type = ContactType.CALL,
                status = ContactStatus.COMPLETED,
                purpose = ContactPurpose.OTHER,
                notes = "Contacto automático de alerta"
            )
        )
    }

    @Transactional
    fun createAlert(request: CreateAlertRequest): AlertResponse {
        val healthCenter = healthCenterRepository.findById(request.healthCenterId)
            .orElseThrow {
                EntityNotFoundException("Health center not found with id: ${request.healthCenterId}")
            }

        val contact = when {
            request.contactId != null -> contactRepository.findById(request.contactId).orElseGet { getOrCreateDefaultContact() }
            request.patientId != null -> {
                val patient = patientRepository.findById(request.patientId).orElse(null)
                if (patient != null) {
                    contactRepository.findAll().find { it.patient.id == patient.id }
                        ?: contactRepository.save(
                            Contact(
                                patient = patient,
                                type = ContactType.CALL,
                                status = ContactStatus.COMPLETED,
                                purpose = ContactPurpose.OTHER,
                                notes = "Contacto de alerta creada"
                            )
                        )
                } else getOrCreateDefaultContact()
            }
            else -> getOrCreateDefaultContact()
        }

        val agent = request.createdByAgentId?.let {
            findAgentByIdOrUserId(it)
        } ?: agentRepository.findAll().firstOrNull()
          ?: throw EntityNotFoundException("No agent registered in system")

        val nextSeq = try {
            jdbcTemplate.queryForObject("SELECT nextval('alert_ticket_seq')", Long::class.java) ?: (System.currentTimeMillis() % 10000)
        } catch (e: Exception) {
            (System.currentTimeMillis() % 10000)
        }
        val ticketNo = "ALT-2026-$nextSeq"

        val alert = Alert(
            ticketNumber = ticketNo,
            healthCenter = healthCenter,
            contact = contact,
            createdBy = agent,
            title = request.title,
            description = request.description,
            status = AlertStatus.ACTIVE,
            severity = AlertSeverity.HIGH,
            category = "GENERAL"
        )

        val saved = alertRepository.saveAndFlush(alert)

        // Automatically add initial CREATED event to timeline
        val initialEvent = AlertEvent(
            alert = saved,
            agent = agent,
            eventType = AlertEventType.CREATED,
            title = "Alerta Reportada [Ticket ${saved.ticketNumber}]",
            description = "Alerta iniciada por ${agent.fullName}: \"${saved.title}\""
        )
        alertEventRepository.save(initialEvent)

        // Extract entity data within @Transactional scope, then dispatch async webhook
        val patient = saved.contact.patient
        n8nWebhookService.notifyAlertCreated(
            ticketNumber = saved.ticketNumber ?: "",
            nombre       = patient.fullName,
            dni          = patient.dni ?: "",
            celular      = patient.primaryPhone,
            titulo       = saved.title,
            descripcion  = saved.description,
            centroSalud  = saved.healthCenter.name,
            gravedad     = saved.severity.name
        )

        logger.info("Created alert: ticket={}, id={}, healthCenterId={}", saved.ticketNumber, saved.id, healthCenter.id)
        return saved.toResponse()
    }

    @Transactional
    fun resolveAlert(alertId: UUID, request: ResolveAlertRequest): AlertResponse {
        val alert = alertRepository.findById(alertId)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $alertId") }

        if (alert.status != AlertStatus.ACTIVE) {
            throw IllegalStateException("Cannot resolve alert $alertId: current status is ${alert.status}, expected ACTIVE")
        }

        val agent = request.resolvedByAgentId?.let {
            findAgentByIdOrUserId(it)
        } ?: alert.createdBy

        alert.status = AlertStatus.RESOLVED
        alert.resolvedAt = LocalDateTime.now()
        alert.resolvedBy = agent

        val saved = alertRepository.saveAndFlush(alert)

        // Record RESOLVED event on timeline
        val resolveEvent = AlertEvent(
            alert = saved,
            agent = agent,
            eventType = AlertEventType.RESOLVED,
            title = "Alerta Resuelta",
            description = "El caso fue marcado como resuelto por ${agent.fullName}."
        )
        alertEventRepository.save(resolveEvent)

        // Extract entity data within @Transactional scope, then dispatch async webhook
        val patientR = saved.contact.patient
        n8nWebhookService.notifyAlertResolved(
            ticketNumber = saved.ticketNumber ?: "",
            nombre       = patientR.fullName,
            dni          = patientR.dni ?: "",
            celular      = patientR.primaryPhone,
            titulo       = saved.title,
            descripcion  = saved.description,
            centroSalud  = saved.healthCenter.name,
            gravedad     = saved.severity.name
        )

        logger.info("Resolved alert: ticket={}, id={}", saved.ticketNumber, saved.id)
        return saved.toResponse()
    }

    fun getAlertsByHealthCenter(healthCenterId: UUID): List<AlertResponse> {
        return alertRepository.findByHealthCenterId(healthCenterId).map { it.toResponse() }
    }

    fun getActiveAlerts(): List<AlertResponse> {
        return alertRepository.findByStatus(AlertStatus.ACTIVE).map { it.toResponse() }
    }

    fun getAlertsByStatus(status: AlertStatus): List<AlertResponse> {
        return alertRepository.findByStatus(status).map { it.toResponse() }
    }

    fun getAlertsByAgent(agentId: UUID): List<AlertResponse> {
        return alertRepository.findByCreatedById(agentId).map { it.toResponse() }
    }

    fun getAlertById(id: UUID): AlertResponse {
        val alert = alertRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $id") }
        return alert.toResponse()
    }

    fun getAlertByTicketNumber(ticketNumber: String): AlertResponse {
        val alert = alertRepository.findByTicketNumber(ticketNumber.trim())
            ?: throw EntityNotFoundException("Alert not found with ticket number: $ticketNumber")
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

        request.status?.let { alert.status = it }
        request.underReview?.let { alert.underReview = it }

        var isDerivedUpdated = false
        request.derivedTo?.let {
            alert.derivedTo = it
            isDerivedUpdated = true
        }
        request.derivationNotes?.let {
            alert.derivationNotes = it
            isDerivedUpdated = true
        }

        val saved = alertRepository.saveAndFlush(alert)

        if (isDerivedUpdated && alert.derivedTo != null) {
            val deriveEvent = AlertEvent(
                alert = saved,
                agent = saved.createdBy,
                eventType = AlertEventType.DERIVED,
                title = "Alerta Derivada a: ${saved.derivedTo}",
                description = saved.derivationNotes ?: "Se derivó la gestión a la entidad externa indicada."
            )
            alertEventRepository.save(deriveEvent)

            // Extract entity data within @Transactional scope, then dispatch async webhook
            val patientD = saved.contact.patient
            n8nWebhookService.notifyAlertDerived(
                ticketNumber = saved.ticketNumber ?: "",
                nombre       = patientD.fullName,
                dni          = patientD.dni ?: "",
                celular      = patientD.primaryPhone,
                titulo       = saved.title,
                derivedTo    = saved.derivedTo ?: "entidad externa",
                centroSalud  = saved.healthCenter.name,
                gravedad     = saved.severity.name
            )
        }

        return saved.toResponse()
    }

    @Transactional
    fun deleteAlert(id: UUID) {
        val alert = alertRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $id") }
        alertRepository.delete(alert)
    }

    // ============================================================
    // Timeline Events & AI Summary Methods
    // ============================================================

    fun getAlertEvents(alertId: UUID): List<AlertEventResponse> {
        if (!alertRepository.existsById(alertId)) {
            throw EntityNotFoundException("Alert not found with id: $alertId")
        }
        return alertEventRepository.findByAlertIdOrderByCreatedAtAsc(alertId).map { it.toResponse() }
    }

    @Transactional
    fun addAlertEvent(alertId: UUID, request: AddAlertEventRequest): AlertEventResponse {
        val alert = alertRepository.findById(alertId)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $alertId") }

        val agent = request.agentId?.let {
            findAgentByIdOrUserId(it)
        } ?: alert.createdBy

        val event = AlertEvent(
            alert = alert,
            agent = agent,
            eventType = AlertEventType.COMMENT,
            title = request.title,
            description = request.description
        )

        val saved = alertEventRepository.saveAndFlush(event)
        return saved.toResponse()
    }

    @Transactional
    fun generateAISummary(alertId: UUID): AlertResponse {
        val alert = alertRepository.findById(alertId)
            .orElseThrow { EntityNotFoundException("Alert not found with id: $alertId") }

        val events = alertEventRepository.findByAlertIdOrderByCreatedAtAsc(alertId)
        val summaryText = aiService.generateExecutiveSummary(alert, events)

        alert.aiSummary = summaryText
        val saved = alertRepository.saveAndFlush(alert)

        // Save AI event in timeline
        val aiEvent = AlertEvent(
            alert = saved,
            agent = saved.createdBy,
            eventType = AlertEventType.AI_SUMMARY_GENERATED,
            title = "Resumen Ejecutivo Generado por IA",
            description = summaryText
        )
        alertEventRepository.save(aiEvent)

        return saved.toResponse()
    }

    private fun AlertEvent.toResponse(): AlertEventResponse = AlertEventResponse(
        id = id ?: throw IllegalStateException("AlertEvent ID is null"),
        alertId = alert.id ?: throw IllegalStateException("Alert ID is null on event"),
        agentId = agent?.id,
        agentName = agent?.fullName,
        eventType = eventType,
        title = title,
        description = description,
        createdAt = createdAt ?: LocalDateTime.now()
    )

    private fun Alert.toResponse(): AlertResponse = AlertResponse(
        id = id ?: throw IllegalStateException("Alert ID is null after save"),
        ticketNumber = ticketNumber,
        healthCenterId = healthCenter.id ?: throw IllegalStateException("HealthCenter ID is null on alert"),
        healthCenterName = healthCenter.name,
        contactId = contact.id ?: throw IllegalStateException("Contact ID is null on alert"),
        patientId = contact.patient.id,
        patientFullName = contact.patient.fullName,
        patientDni = contact.patient.dni,
        patientPhone = contact.patient.primaryPhone,
        createdByAgentId = createdBy.id ?: throw IllegalStateException("CreatedBy agent ID is null on alert"),
        createdByAgentName = createdBy.fullName,
        title = title,
        description = description,
        status = status,
        severity = severity,
        category = category,
        underReview = underReview,
        derivedTo = derivedTo,
        derivationNotes = derivationNotes,
        aiSummary = aiSummary,
        resolvedAt = resolvedAt,
        resolvedByAgentId = resolvedBy?.id,
        resolvedByAgentName = resolvedBy?.fullName,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
