package com.hazardev.fpc_back.patient.application

import tools.jackson.databind.ObjectMapper
import com.hazardev.fpc_back.patient.application.dto.PatientSummaryResponse
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.domain.PatientSummary
import com.hazardev.fpc_back.patient.domain.PatientSummaryStatus
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSummaryRepository
import com.hazardev.fpc_back.patient.infrastructure.gemini.GeminiClient
import com.hazardev.fpc_back.patient.infrastructure.gemini.PatientSummaryGenerationException
import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import java.time.LocalDateTime
import java.util.Optional
import java.util.UUID
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PatientSummaryOnDemandServiceTest {
    private val objectMapper = ObjectMapper()
    private val patientRepository: PatientRepository = mock()
    private val patientSummaryRepository: PatientSummaryRepository = mock()
    private val payloadService: PatientSummaryPayloadService = mock()
    private val geminiClient: GeminiClient = mock()
    private val generationService: PatientSummaryGenerationService = mock()
    private val queryService: PatientSummaryQueryService = mock()

    private val service = PatientSummaryOnDemandService(
        patientRepository = patientRepository,
        patientSummaryRepository = patientSummaryRepository,
        payloadService = payloadService,
        geminiClient = geminiClient,
        generationService = generationService,
        queryService = queryService
    )

    private val patientId = UUID.randomUUID()
    private val sourceUpdatedAt = LocalDateTime.of(2026, 6, 2, 10, 0)
    private val generatedAt = LocalDateTime.of(2026, 6, 2, 10, 5)
    private val patient = Patient(
        id = patientId,
        fullName = "Paciente Demo",
        dni = "12345678",
        primaryPhone = "999999999",
        role = PatientRole.PATIENT,
        status = PatientStatus.ACTIVE
    ).apply {
        summarySourceUpdatedAt = sourceUpdatedAt
    }

    @Test
    fun `generates and returns ready summary on demand`() {
        val payload = PatientSummaryPayload(patientId, sourceUpdatedAt, "{\"paciente\":{}}")
        val summaryEntity = readySummaryEntity()
        val summaryResponse = PatientSummaryResponse(
            status = PatientSummaryStatus.READY.name,
            stale = false,
            updatedAt = generatedAt,
            content = objectMapper.readTree("{\"ok\":true}"),
            lastErrorCode = null
        )

        whenever(patientRepository.findByDni(patient.dni!!)).thenReturn(patient)
        whenever(patientSummaryRepository.existsById(patientId)).thenReturn(true)
        whenever(payloadService.buildSummaryPayload(patientId)).thenReturn(payload)
        whenever(geminiClient.generatePatientSummary(payload.patientDataJson)).thenReturn("{\"ok\":true}")
        whenever(patientSummaryRepository.findById(patientId)).thenReturn(Optional.of(summaryEntity))
        whenever(queryService.getSummaryResponse(patient)).thenReturn(summaryResponse)

        val result = service.generateByDni(patient.dni!!)

        assertEquals(HttpStatus.OK, result.httpStatus)
        assertEquals("READY", result.body.result)
        assertTrue(result.body.metadata.generatedOnDemand)
        assertFalse(result.body.metadata.fallbackUsed)
        verify(generationService).markSuccess(patientId, sourceUpdatedAt, "{\"ok\":true}")
        verify(generationService, never()).markTemporaryFailure(any(), any())
    }

    @Test
    fun `returns stored fallback when on demand generation fails`() {
        val exception = PatientSummaryGenerationException(
            code = "RATE_LIMIT",
            message = "Temporal failure",
            temporary = true
        )
        val summaryEntity = readySummaryEntity().apply {
            status = PatientSummaryStatus.RETRY_WAIT
            retryCount = 1
            lastAttemptAt = this@PatientSummaryOnDemandServiceTest.generatedAt.plusMinutes(1)
            nextAttemptAt = this@PatientSummaryOnDemandServiceTest.generatedAt.plusMinutes(2)
            lastErrorCode = "RATE_LIMIT"
        }
        val summaryResponse = PatientSummaryResponse(
            status = PatientSummaryStatus.RETRY_WAIT.name,
            stale = true,
            updatedAt = generatedAt,
            content = objectMapper.readTree("{\"ok\":true}"),
            lastErrorCode = "RATE_LIMIT"
        )

        whenever(patientRepository.findByDni(patient.dni!!)).thenReturn(patient)
        whenever(patientSummaryRepository.existsById(patientId)).thenReturn(true)
        whenever(payloadService.buildSummaryPayload(patientId)).thenReturn(PatientSummaryPayload(patientId, sourceUpdatedAt, "{}"))
        whenever(geminiClient.generatePatientSummary("{}")).thenThrow(exception)
        whenever(patientSummaryRepository.findById(patientId)).thenReturn(Optional.of(summaryEntity))
        whenever(queryService.getSummaryResponse(patient)).thenReturn(summaryResponse)

        val result = service.generateByDni(patient.dni!!)

        assertEquals(HttpStatus.OK, result.httpStatus)
        assertEquals("FALLBACK", result.body.result)
        assertTrue(result.body.metadata.fallbackUsed)
        assertEquals("RATE_LIMIT", result.body.error?.code)
        verify(generationService).markTemporaryFailure(patientId, exception)
    }

    @Test
    fun `returns accepted pending response when no valid fallback exists`() {
        val exception = PatientSummaryGenerationException(
            code = "NETWORK_ERROR",
            message = "No response from Gemini",
            temporary = true
        )
        val pendingSummary = PatientSummary(
            patient = patient,
            status = PatientSummaryStatus.RETRY_WAIT,
            retryCount = 1,
            nextAttemptAt = generatedAt.plusMinutes(2)
        ).apply {
            lastAttemptAt = this@PatientSummaryOnDemandServiceTest.generatedAt.plusMinutes(1)
            lastErrorCode = "NETWORK_ERROR"
            lastErrorMessage = "No response from Gemini"
        }
        val summaryResponse = PatientSummaryResponse(
            status = PatientSummaryStatus.RETRY_WAIT.name,
            stale = true,
            updatedAt = null,
            content = null,
            lastErrorCode = "NETWORK_ERROR"
        )

        whenever(patientRepository.findByDni(patient.dni!!)).thenReturn(patient)
        whenever(patientSummaryRepository.existsById(patientId)).thenReturn(false)
        whenever(patientSummaryRepository.save(any())).thenReturn(pendingSummary)
        whenever(payloadService.buildSummaryPayload(patientId)).thenReturn(PatientSummaryPayload(patientId, sourceUpdatedAt, "{}"))
        whenever(geminiClient.generatePatientSummary("{}")).thenThrow(exception)
        whenever(patientSummaryRepository.findById(patientId)).thenReturn(Optional.of(pendingSummary))
        whenever(queryService.getSummaryResponse(patient)).thenReturn(summaryResponse)

        val result = service.generateByDni(patient.dni!!)

        assertEquals(HttpStatus.ACCEPTED, result.httpStatus)
        assertEquals("PENDING", result.body.result)
        assertFalse(result.body.metadata.fallbackUsed)
        assertEquals("NETWORK_ERROR", result.body.error?.code)
        verify(patientSummaryRepository).save(any())
        verify(generationService).markTemporaryFailure(eq(patientId), eq(exception))
    }

    private fun readySummaryEntity(): PatientSummary {
        return PatientSummary(
            patient = patient,
            status = PatientSummaryStatus.READY,
            summaryJson = "{\"ok\":true}",
            retryCount = 0
        ).apply {
            generatedAt = generatedAt
            generatedFromSourceUpdatedAt = sourceUpdatedAt
        }
    }
}
