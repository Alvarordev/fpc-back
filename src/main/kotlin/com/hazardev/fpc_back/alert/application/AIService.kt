package com.hazardev.fpc_back.alert.application

import com.hazardev.fpc_back.alert.domain.Alert
import com.hazardev.fpc_back.alert.domain.AlertEvent
import com.hazardev.fpc_back.alert.domain.AlertEventType
import com.hazardev.fpc_back.shared.domain.AlertStatus
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class AIService(
    @Value("\${ai.api-key:}") private val apiKey: String
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

    fun generateExecutiveSummary(alert: Alert, events: List<AlertEvent>): String {
        val healthCenterName = alert.healthCenter.name
        val severityStr = alert.severity.name
        val statusStr = if (alert.status == AlertStatus.ACTIVE) "ACTIVA" else "RESUELTA"
        
        val commentsCount = events.count { it.eventType == AlertEventType.COMMENT }
        val derivedEvent = events.firstOrNull { it.eventType == AlertEventType.DERIVED }
        
        val sb = StringBuilder()
        sb.append("📋 RESUMEN EJECUTIVO IA:\n")
        sb.append("La alerta '${alert.title}' reportada en '${healthCenterName}' se encuentra actualmente en estado ${statusStr} (Severidad: ${severityStr}). ")
        
        val created = alert.createdAt
        if (created != null) {
            sb.append("Iniciada el ${created.format(dateFormatter)} por ${alert.createdBy.fullName}. ")
        }
        
        if (derivedEvent != null || alert.derivedTo != null) {
            val target = alert.derivedTo ?: derivedEvent?.description ?: "entidad externa"
            sb.append("Caso derivado a: '${target}'. ")
            val notes = alert.derivationNotes
            if (notes != null && notes.isNotBlank()) {
                sb.append("Nota de derivación: \"${notes}\". ")
            }
        }
        
        if (commentsCount > 0) {
            sb.append("Cuenta con ${commentsCount} avance(s) registrado(s) en la línea de tiempo. ")
        }
        
        val resolvedDate = alert.resolvedAt
        if (alert.status == AlertStatus.RESOLVED && resolvedDate != null) {
            val resolvedByName = alert.resolvedBy?.fullName ?: "Agente"
            sb.append("Atención concluida y resuelta el ${resolvedDate.format(dateFormatter)} por ${resolvedByName}.")
        } else {
            sb.append("Se recomienda seguimiento activo con el centro de salud para la resolución del incidente.")
        }
        
        return sb.toString()
    }
}
