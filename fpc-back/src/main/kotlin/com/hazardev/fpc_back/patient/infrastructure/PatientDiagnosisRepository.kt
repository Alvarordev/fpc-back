package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientDiagnosis
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientDiagnosisRepository : JpaRepository<PatientDiagnosis, Long> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: Long): List<PatientDiagnosis>

    fun findByPatientIdAndIsCurrentTrue(patientId: Long): List<PatientDiagnosis>
}
