package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.patient.infrastructure.gemini.GeminiClient
import org.springframework.stereotype.Service

@Service
class PatientSummaryService(
    private val geminiClient: GeminiClient
) {
    /**
     * Genera un resumen completo en formato JSON para un paciente a través de su DNI.
     */
    fun generateSummaryByDni(dni: String): String {
        return geminiClient.generatePatientSummary(dni)
    }
}
