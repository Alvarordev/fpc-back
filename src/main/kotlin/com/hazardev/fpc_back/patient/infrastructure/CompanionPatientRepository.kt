package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.CompanionPatient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface CompanionPatientRepository : JpaRepository<CompanionPatient, UUID> {

    fun findByCompanionId(companionId: UUID): List<CompanionPatient>

    fun findByPatientId(patientId: UUID): List<CompanionPatient>

    fun findByPatientIdAndCompanionId(patientId: UUID, companionId: UUID): CompanionPatient?
}
