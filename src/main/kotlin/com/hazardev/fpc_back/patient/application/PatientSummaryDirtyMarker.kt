package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.domain.PatientSummary
import com.hazardev.fpc_back.patient.domain.PatientSummaryStatus
import com.hazardev.fpc_back.patient.infrastructure.PatientSummaryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PatientSummaryDirtyMarker(
    private val patientSummaryRepository: PatientSummaryRepository
) {

    @Transactional
    fun markDirty(patient: Patient, changedAt: LocalDateTime = LocalDateTime.now()) {
        patient.summarySourceUpdatedAt = changedAt

        val summary = patientSummaryRepository.findById(patient.id!!)
            .orElseGet {
                PatientSummary(
                    patient = patient,
                    status = PatientSummaryStatus.PENDING,
                    nextAttemptAt = changedAt
                )
            }

        summary.status = PatientSummaryStatus.PENDING
        summary.retryCount = 0
        summary.nextAttemptAt = changedAt
        summary.processingStartedAt = null
        summary.lastAttemptAt = null
        summary.lastErrorCode = null
        summary.lastErrorMessage = null

        patientSummaryRepository.save(summary)
    }
}
