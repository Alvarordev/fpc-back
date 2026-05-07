package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientSisAffiliation
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientSisAffiliationRepository : JpaRepository<PatientSisAffiliation, Long> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: Long): List<PatientSisAffiliation>
}
