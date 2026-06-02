package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.patient.domain.PatientSummaryStatus
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSummaryRepository
import com.hazardev.fpc_back.patient.infrastructure.gemini.GeminiClient
import com.hazardev.fpc_back.patient.infrastructure.gemini.PatientSummaryGenerationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Service
class PatientSummaryGenerationService(
    private val payloadService: PatientSummaryPayloadService,
    private val patientRepository: PatientRepository,
    private val patientSummaryRepository: PatientSummaryRepository,
    private val geminiClient: GeminiClient,
    private val properties: PatientSummaryProperties
) {
    private val logger = LoggerFactory.getLogger(PatientSummaryGenerationService::class.java)

    fun generate(patientId: UUID) {
        try {
            val payload = payloadService.buildSummaryPayload(patientId)
            val summaryJson = geminiClient.generatePatientSummary(payload.patientDataJson)
            markSuccess(patientId, payload.sourceUpdatedAt, summaryJson)
        } catch (ex: PatientSummaryGenerationException) {
            if (ex.temporary) {
                markTemporaryFailure(patientId, ex)
            } else {
                markPermanentFailure(patientId, ex)
            }
        } catch (ex: Exception) {
            markTemporaryFailure(
                patientId,
                PatientSummaryGenerationException(
                    code = "UNEXPECTED_PROCESSING_ERROR",
                    message = "Unexpected patient summary processing error: ${ex.message}",
                    temporary = true,
                    cause = ex
                )
            )
        }
    }

    @Transactional
    fun markSuccess(patientId: UUID, sourceUpdatedAt: LocalDateTime?, summaryJson: String) {
        val patient = patientRepository.findById(patientId).orElseThrow()
        val summary = patientSummaryRepository.findByPatientIdWithLock(patientId).orElseThrow()
        val now = LocalDateTime.now()

        summary.summaryJson = summaryJson
        summary.generatedAt = now
        summary.generatedFromSourceUpdatedAt = sourceUpdatedAt
        summary.retryCount = 0
        summary.processingStartedAt = null
        summary.lastAttemptAt = now
        summary.lastErrorCode = null
        summary.lastErrorMessage = null

        if (patient.summarySourceUpdatedAt != sourceUpdatedAt) {
            summary.status = PatientSummaryStatus.PENDING
            summary.nextAttemptAt = now
            logger.info("Patient {} changed while summary was processing; queued regeneration", patientId)
        } else {
            summary.status = PatientSummaryStatus.READY
            summary.nextAttemptAt = null
        }

        patientSummaryRepository.save(summary)
    }

    @Transactional
    fun markTemporaryFailure(patientId: UUID, ex: PatientSummaryGenerationException) {
        val summary = patientSummaryRepository.findByPatientIdWithLock(patientId).orElseThrow()
        val nextRetryCount = summary.retryCount + 1
        val now = LocalDateTime.now()

        summary.retryCount = nextRetryCount
        summary.processingStartedAt = null
        summary.lastAttemptAt = now
        summary.lastErrorCode = ex.code
        summary.lastErrorMessage = ex.message

        if (nextRetryCount > properties.maxRetries) {
            summary.status = PatientSummaryStatus.FAILED_PERMANENT
            summary.nextAttemptAt = null
            logger.warn("Patient summary retries exhausted for patient {} with code {}", patientId, ex.code)
        } else {
            summary.status = PatientSummaryStatus.RETRY_WAIT
            summary.nextAttemptAt = now.plusSeconds(computeBackoffSeconds(nextRetryCount))
        }

        patientSummaryRepository.save(summary)
    }

    @Transactional
    fun markPermanentFailure(patientId: UUID, ex: PatientSummaryGenerationException) {
        val summary = patientSummaryRepository.findByPatientIdWithLock(patientId).orElseThrow()
        val now = LocalDateTime.now()
        summary.status = PatientSummaryStatus.FAILED_PERMANENT
        summary.processingStartedAt = null
        summary.lastAttemptAt = now
        summary.nextAttemptAt = null
        summary.lastErrorCode = ex.code
        summary.lastErrorMessage = ex.message
        patientSummaryRepository.save(summary)
    }

    private fun computeBackoffSeconds(retryCount: Int): Long {
        return when (retryCount) {
            1 -> 30
            2 -> 60
            3 -> 120
            4 -> 240
            5 -> 480
            6 -> 900
            else -> 1800
        }
    }
}
