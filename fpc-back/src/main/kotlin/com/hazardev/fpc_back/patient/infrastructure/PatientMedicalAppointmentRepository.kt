package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.PatientMedicalAppointment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PatientMedicalAppointmentRepository : JpaRepository<PatientMedicalAppointment, UUID> {

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<PatientMedicalAppointment>
}
