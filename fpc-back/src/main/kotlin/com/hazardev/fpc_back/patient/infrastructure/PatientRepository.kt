package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.Patient
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PatientRepository : JpaRepository<Patient, Long>
