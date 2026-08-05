package com.hazardev.fpc_back.shared.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class N8nWebhookService(
    @Value("\${n8n.webhook.url:https://jaffjos-n8n.3ezxho.easypanel.host/webhook/notificacion}")
    private val webhookUrl: String
) {
    companion object {
        private val logger = LoggerFactory.getLogger(N8nWebhookService::class.java)
    }

    private val restClient = RestClient.create()

    /**
     * Webhook event for Alerta creada.
     * All parameters are plain Strings — data must be extracted from JPA entities
     * by the caller INSIDE its @Transactional scope before calling this method.
     * This guarantees that (a) lazy associations are loaded and (b) the ticket
     * has already been persisted when the async HTTP call fires.
     */
    @Async
    fun notifyAlertCreated(
        ticketNumber: String,
        nombre: String,
        dni: String,
        celular: String,
        titulo: String,
        descripcion: String,
        centroSalud: String,
        gravedad: String
    ) {
        val query = mapOf(
            "nombre"       to nombre,
            "DNI"          to dni,
            "celular"      to celular,
            "titulo"       to titulo,
            "descripcion"  to descripcion,
            "centro_salud" to centroSalud,
            "gravedad"     to gravedad,
            "ticket"       to ticketNumber
        )
        val payload = mapOf("query" to query, "var" to "Alerta")
        sendPayload(payload, "Alerta", ticketNumber)
    }

    /**
     * Webhook event for Alerta resuelta.
     */
    @Async
    fun notifyAlertResolved(
        ticketNumber: String,
        nombre: String,
        dni: String,
        celular: String,
        titulo: String,
        descripcion: String,
        centroSalud: String,
        gravedad: String
    ) {
        val query = mapOf(
            "nombre"       to nombre,
            "DNI"          to dni,
            "celular"      to celular,
            "titulo"       to "Alerta resuelta: $titulo",
            "descripcion"  to descripcion,
            "centro_salud" to centroSalud,
            "gravedad"     to gravedad,
            "ticket"       to ticketNumber
        )
        val payload = mapOf("query" to query, "var" to "AlertaResuelta")
        sendPayload(payload, "AlertaResuelta", ticketNumber)
    }

    /**
     * Webhook event for Alerta derivada.
     */
    @Async
    fun notifyAlertDerived(
        ticketNumber: String,
        nombre: String,
        dni: String,
        celular: String,
        titulo: String,
        derivedTo: String,
        centroSalud: String,
        gravedad: String
    ) {
        val query = mapOf(
            "nombre"       to nombre,
            "DNI"          to dni,
            "celular"      to celular,
            "titulo"       to "Alerta derivada: $titulo",
            "descripcion"  to "Alerta derivada a: $derivedTo",
            "centro_salud" to centroSalud,
            "gravedad"     to gravedad,
            "ticket"       to ticketNumber
        )
        val payload = mapOf("query" to query, "var" to "AlertaDerivar")
        sendPayload(payload, "AlertaDerivar", ticketNumber)
    }

    /**
     * Webhook event for Cita (Medical Appointment).
     * All parameters are plain Strings — extracted from JPA entities by the caller.
     */
    @Async
    fun notifyAppointmentCreated(
        nombre: String,
        dni: String,
        celular: String,
        correo: String,
        motivo: String,
        fecha: String,
        hora: String,
        especialidad: String,
        ref: String
    ) {
        val query = mapOf(
            "nombre"      to nombre,
            "DNI"         to dni,
            "celular"     to celular,
            "correo"      to correo,
            "motivo"      to motivo,
            "fecha"       to fecha,
            "hora"        to hora,
            "especialidad" to especialidad
        )
        val payload = mapOf("query" to query, "var" to "Cita")
        sendPayload(payload, "Cita", ref)
    }

    /**
     * Webhook event for Patient Registration.
     * All parameters are plain Strings — extracted from JPA entities by the caller.
     */
    @Async
    fun notifyPatientRegistered(
        nombre: String,
        dni: String,
        celular: String,
        correo: String,
        diagnostico: String,
        condicion: String,
        ref: String
    ) {
        val query = mapOf(
            "nombre"      to nombre,
            "DNI"         to dni,
            "celular"     to celular,
            "correo"      to correo,
            "diagnostico" to diagnostico,
            "condicion"   to condicion
        )
        val payload = mapOf("query" to query, "var" to "Registro")
        sendPayload(payload, "Registro", ref)
    }

    private fun sendPayload(payload: Any, eventType: String, referenceId: String) {
        if (webhookUrl.isBlank()) {
            logger.debug("n8n Webhook URL is not configured. Skipping webhook for event: {}", eventType)
            return
        }
        try {
            restClient.post()
                .uri(webhookUrl)
                .body(payload)
                .retrieve()
                .toBodilessEntity()
            logger.info("Successfully sent n8n webhook event '{}' for ref {}", eventType, referenceId)
        } catch (e: Exception) {
            logger.warn("Failed to send n8n webhook event '{}' for ref {}: {}", eventType, referenceId, e.message)
        }
    }
}
