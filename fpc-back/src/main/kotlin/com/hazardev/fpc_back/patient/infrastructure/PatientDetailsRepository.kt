package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientDetails
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientDetailsRepository : JpaRepository<PatientDetails, UUID> {

    fun findByPatientId(patientId: UUID): PatientDetails?
}
