package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientDiagnosis
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientDiagnosisRepository : JpaRepository<PatientDiagnosis, UUID> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<PatientDiagnosis>

    fun findByPatientIdAndIsCurrentTrue(patientId: UUID): List<PatientDiagnosis>
}
