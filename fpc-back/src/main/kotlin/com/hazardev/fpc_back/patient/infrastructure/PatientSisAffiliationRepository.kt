package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientSisAffiliation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientSisAffiliationRepository : JpaRepository<PatientSisAffiliation, UUID> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<PatientSisAffiliation>
}
