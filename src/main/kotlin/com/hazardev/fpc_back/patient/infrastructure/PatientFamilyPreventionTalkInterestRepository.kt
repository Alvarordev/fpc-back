package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientFamilyPreventionTalkInterest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientFamilyPreventionTalkInterestRepository : JpaRepository<PatientFamilyPreventionTalkInterest, UUID> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<PatientFamilyPreventionTalkInterest>
}
