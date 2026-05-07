package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.CompanionPatient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CompanionPatientRepository : JpaRepository<CompanionPatient, Long> {

    fun findByCompanionId(companionId: Long): List<CompanionPatient>

    fun findByPatientId(patientId: Long): List<CompanionPatient>

    fun findByPatientIdAndCompanionId(patientId: Long, companionId: Long): CompanionPatient?
}
