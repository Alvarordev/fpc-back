package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.patient.application.dto.PatientSummaryOnDemandError
import com.hazardev.fpc_back.patient.application.dto.PatientSummaryOnDemandMetadata
import com.hazardev.fpc_back.patient.application.dto.PatientSummaryOnDemandResponse
import com.hazardev.fpc_back.patient.application.dto.PatientSummaryResponse
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.domain.PatientSummary
import com.hazardev.fpc_back.patient.domain.PatientSummaryStatus
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSummaryRepository
import com.hazardev.fpc_back.patient.infrastructure.gemini.GeminiClient
import com.hazardev.fpc_back.patient.infrastructure.gemini.PatientSummaryGenerationException
import jakarta.persistence.EntityNotFoundException
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class PatientSummaryOnDemandService(
    private val patientRepository: PatientRepository,
    private val patientSummaryRepository: PatientSummaryRepository,
    private val payloadService: PatientSummaryPayloadService,
    private val geminiClient: GeminiClient,
    private val generationService: PatientSummaryGenerationService,
    private val queryService: PatientSummaryQueryService
) {

    fun generateByDni(dni: String): PatientSummaryOnDemandResult {
        val patient = patientRepository.findByDni(dni)
            ?: throw EntityNotFoundException("No se encontro un paciente con DNI: $dni")
        val patientId = patient.id!!
        ensureSummaryRecord(patient)

        return try {
            val payload = payloadService.buildSummaryPayload(patientId)
            val summaryJson = geminiClient.generatePatientSummary(payload.patientDataJson)
            generationService.markSuccess(patientId, payload.sourceUpdatedAt, summaryJson)
            buildResult(patient, result = "READY", generatedOnDemand = true, fallbackUsed = false)
        } catch (ex: PatientSummaryGenerationException) {
            handleGenerationFailure(patient, ex)
        } catch (ex: Exception) {
            handleGenerationFailure(
                patient,
                PatientSummaryGenerationException(
                    code = "UNEXPECTED_PROCESSING_ERROR",
                    message = "Unexpected patient summary processing error: ${ex.message}",
                    temporary = true,
                    cause = ex
                )
            )
        }
    }

    private fun handleGenerationFailure(
        patient: Patient,
        ex: PatientSummaryGenerationException
    ): PatientSummaryOnDemandResult {
        val patientId = patient.id!!
        if (ex.temporary) {
            generationService.markTemporaryFailure(patientId, ex)
        } else {
            generationService.markPermanentFailure(patientId, ex)
        }

        val storedSummary = queryService.getSummaryResponse(patient)
        val hasFallback = storedSummary.content != null && storedSummary.updatedAt != null
        val response = buildResponse(
            patient = patient,
            summary = storedSummary,
            summaryEntity = patientSummaryRepository.findById(patientId).orElse(null),
            result = if (hasFallback) "FALLBACK" else "PENDING",
            generatedOnDemand = false,
            fallbackUsed = hasFallback,
            error = PatientSummaryOnDemandError(
                code = ex.code,
                message = ex.message,
                temporary = ex.temporary
            )
        )

        return PatientSummaryOnDemandResult(
            httpStatus = if (hasFallback) HttpStatus.OK else HttpStatus.ACCEPTED,
            body = response
        )
    }

    private fun buildResult(
        patient: Patient,
        result: String,
        generatedOnDemand: Boolean,
        fallbackUsed: Boolean
    ): PatientSummaryOnDemandResult {
        val summaryEntity = patientSummaryRepository.findById(patient.id!!).orElse(null)
        val response = buildResponse(
            patient = patient,
            summary = queryService.getSummaryResponse(patient),
            summaryEntity = summaryEntity,
            result = result,
            generatedOnDemand = generatedOnDemand,
            fallbackUsed = fallbackUsed,
            error = null
        )

        return PatientSummaryOnDemandResult(HttpStatus.OK, response)
    }

    private fun buildResponse(
        patient: Patient,
        summary: PatientSummaryResponse,
        summaryEntity: PatientSummary?,
        result: String,
        generatedOnDemand: Boolean,
        fallbackUsed: Boolean,
        error: PatientSummaryOnDemandError?
    ): PatientSummaryOnDemandResponse {
        return PatientSummaryOnDemandResponse(
            patientId = patient.id!!,
            dni = patient.dni.orEmpty(),
            result = result,
            summary = summary,
            metadata = PatientSummaryOnDemandMetadata(
                generatedOnDemand = generatedOnDemand,
                fallbackUsed = fallbackUsed,
                stale = summary.stale,
                summaryUpdatedAt = summary.updatedAt,
                sourceUpdatedAt = patient.summarySourceUpdatedAt,
                generatedFromSourceUpdatedAt = summaryEntity?.generatedFromSourceUpdatedAt,
                retryCount = summaryEntity?.retryCount ?: 0,
                lastAttemptAt = summaryEntity?.lastAttemptAt,
                nextAttemptAt = summaryEntity?.nextAttemptAt,
                lastErrorCode = summary.lastErrorCode
            ),
            error = error
        )
    }

    private fun ensureSummaryRecord(patient: Patient) {
        if (patientSummaryRepository.existsById(patient.id!!)) {
            return
        }

        patientSummaryRepository.save(
            PatientSummary(
                patient = patient,
                status = PatientSummaryStatus.PENDING,
                nextAttemptAt = LocalDateTime.now()
            )
        )
    }
}

data class PatientSummaryOnDemandResult(
    val httpStatus: HttpStatus,
    val body: PatientSummaryOnDemandResponse
)
