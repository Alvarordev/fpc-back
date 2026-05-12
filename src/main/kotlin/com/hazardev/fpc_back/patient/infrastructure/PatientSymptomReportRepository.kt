package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientSymptomReport
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientSymptomReportRepository : JpaRepository<PatientSymptomReport, UUID> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<PatientSymptomReport>
}
