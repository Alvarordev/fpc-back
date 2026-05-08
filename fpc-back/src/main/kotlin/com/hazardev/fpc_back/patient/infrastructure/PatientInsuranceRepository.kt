package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientInsurance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientInsuranceRepository : JpaRepository<PatientInsurance, UUID> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<PatientInsurance>

    fun findByPatientIdAndIsCurrentTrue(patientId: UUID): List<PatientInsurance>
}
