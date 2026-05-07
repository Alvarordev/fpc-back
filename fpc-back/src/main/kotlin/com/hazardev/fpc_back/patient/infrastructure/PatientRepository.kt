package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientRepository : JpaRepository<Patient, Long> {

    fun findByStatus(status: PatientStatus): List<Patient>

    fun findByRole(role: PatientRole): List<Patient>

    fun findByDni(dni: String): Patient?
}
