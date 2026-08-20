package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.patient.domain.PatientSummary
import com.hazardev.fpc_back.patient.domain.PatientSummaryStatus
import com.hazardev.fpc_back.patient.infrastructure.PatientSummaryRepository
import org.slf4j.LoggerFactory
import org.springframework.data.domain.PageRequest
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.UUID

@Component
class PatientSummaryScheduler(
    private val claimService: PatientSummaryClaimService,
    private val worker: PatientSummaryGenerationWorker,
    private val properties: PatientSummaryProperties
) {
    @Scheduled(fixedDelayString = "\${patient.summary.scheduler-delay-ms:5000}")
    fun processPendingSummaries() {
        repeat(properties.maxBatchSize) {
            val claimed = claimService.claimNext() ?: return
            worker.process(claimed.patientId!!)
        }
    }
}

@Service
class PatientSummaryClaimService(
    private val patientSummaryRepository: PatientSummaryRepository,
    private val properties: PatientSummaryProperties
) {

    @Transactional
    fun claimNext(now: LocalDateTime = LocalDateTime.now()): PatientSummary? {
        val staleBefore = now.minusSeconds(properties.processingTimeoutSeconds)
        val candidateIds = patientSummaryRepository.findCandidatePatientIds(
            eligibleStatuses = listOf(PatientSummaryStatus.PENDING, PatientSummaryStatus.RETRY_WAIT),
            processingStatus = PatientSummaryStatus.PROCESSING,
            now = now,
            staleBefore = staleBefore,
            pageable = PageRequest.of(0, properties.maxBatchSize)
        )

        for (patientId in candidateIds) {
            val summary = patientSummaryRepository.findByPatientIdWithLock(patientId).orElse(null) ?: continue
            if (!isEligible(summary, now, staleBefore)) {
                continue
            }

            summary.status = PatientSummaryStatus.PROCESSING
            summary.processingStartedAt = now
            summary.lastAttemptAt = now
            summary.nextAttemptAt = null
            return patientSummaryRepository.save(summary)
        }

        return null
    }

    private fun isEligible(summary: PatientSummary, now: LocalDateTime, staleBefore: LocalDateTime): Boolean {
        return when (summary.status) {
            PatientSummaryStatus.PENDING, PatientSummaryStatus.RETRY_WAIT ->
                summary.nextAttemptAt == null || !summary.nextAttemptAt!!.isAfter(now)
            PatientSummaryStatus.PROCESSING ->
                summary.processingStartedAt != null && !summary.processingStartedAt!!.isAfter(staleBefore)
            PatientSummaryStatus.READY, PatientSummaryStatus.FAILED_PERMANENT -> false
        }
    }
}

@Service
class PatientSummaryGenerationWorker(
    private val rateLimiter: GeminiRateLimiter,
    private val generationService: PatientSummaryGenerationService,
    private val patientSummaryRepository: PatientSummaryRepository
) {
    private val logger = LoggerFactory.getLogger(PatientSummaryGenerationWorker::class.java)

    fun process(patientId: UUID) {
        val now = LocalDateTime.now()
        val reservation = rateLimiter.tryAcquire(now)
        if (!reservation.acquired) {
            postponeForRateLimit(patientId, reservation.nextAvailableAt ?: now.plusMinutes(1))
            return
        }

        try {
            generationService.generate(patientId)
        } catch (ex: Exception) {
            logger.error("Unexpected patient summary worker failure for patient {}", patientId, ex)
        }
    }

    @Transactional
    fun postponeForRateLimit(patientId: UUID, nextAttemptAt: LocalDateTime) {
        val summary = patientSummaryRepository.findByPatientIdWithLock(patientId).orElse(null) ?: return
        summary.status = PatientSummaryStatus.RETRY_WAIT
        summary.processingStartedAt = null
        summary.nextAttemptAt = nextAttemptAt
        summary.lastErrorCode = "RATE_LIMIT"
        summary.lastErrorMessage = "Global Gemini rate limit reached"
        patientSummaryRepository.save(summary)
    }
}
