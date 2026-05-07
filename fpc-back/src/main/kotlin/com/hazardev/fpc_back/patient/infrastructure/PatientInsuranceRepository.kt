package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientInsurance
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientInsuranceRepository : JpaRepository<PatientInsurance, Long> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: Long): List<PatientInsurance>

    fun findByPatientIdAndIsCurrentTrue(patientId: Long): List<PatientInsurance>
}
