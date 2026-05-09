package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientTreatment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientTreatmentRepository : JpaRepository<PatientTreatment, UUID> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<PatientTreatment>

    fun findByPatientIdAndIsCurrentTrue(patientId: UUID): List<PatientTreatment>

    fun findByDiagnosisId(diagnosisId: UUID): List<PatientTreatment>
}
