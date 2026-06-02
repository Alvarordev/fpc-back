package com.hazardev.fpc_back.patient.application

import tools.jackson.databind.JsonNode
import tools.jackson.databind.ObjectMapper
import com.hazardev.fpc_back.patient.application.dto.PatientSummaryResponse
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.domain.PatientSummary
import com.hazardev.fpc_back.patient.domain.PatientSummaryStatus
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSummaryRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PatientSummaryQueryService(
    private val patientRepository: PatientRepository,
    private val patientSummaryRepository: PatientSummaryRepository,
    private val objectMapper: ObjectMapper
) {

    fun getSummaryResponse(patient: Patient): PatientSummaryResponse {
        val summary = patientSummaryRepository.findById(patient.id!!).orElse(null)
        return summary?.toResponse(patient.summarySourceUpdatedAt) ?: PatientSummaryResponse(
            status = PatientSummaryStatus.PENDING.name,
            stale = true,
            updatedAt = null,
            content = null
        )
    }

    fun getStoredSummaryJsonByDni(dni: String): String? {
        val patient = patientRepository.findByDni(dni)
            ?: throw EntityNotFoundException("No se encontro un paciente con DNI: $dni")
        return patientSummaryRepository.findById(patient.id!!)
            .orElse(null)
            ?.summaryJson
    }

    private fun PatientSummary.toResponse(sourceUpdatedAt: java.time.LocalDateTime?): PatientSummaryResponse {
        val content = summaryJson?.let(::readJsonNode)
        val stale = generatedFromSourceUpdatedAt == null || sourceUpdatedAt == null || generatedFromSourceUpdatedAt != sourceUpdatedAt
        return PatientSummaryResponse(
            status = status.name,
            stale = stale,
            updatedAt = generatedAt,
            content = content,
            lastErrorCode = lastErrorCode
        )
    }

    private fun readJsonNode(summaryJson: String): JsonNode? {
        return try {
            objectMapper.readTree(summaryJson)
        } catch (_: Exception) {
            null
        }
    }
}
