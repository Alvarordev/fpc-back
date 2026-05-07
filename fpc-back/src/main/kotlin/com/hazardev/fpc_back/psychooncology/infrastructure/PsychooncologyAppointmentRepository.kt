package com.hazardev.fpc_back.psychooncology.infrastructure

import com.hazardev.fpc_back.psychooncology.domain.PsychooncologyAppointment
import com.hazardev.fpc_back.shared.domain.AppointmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PsychooncologyAppointmentRepository : JpaRepository<PsychooncologyAppointment, Long> {

    /**
     * Find all appointments for a specific patient.
     */
    fun findByPatientId(patientId: Long): List<PsychooncologyAppointment>

    /**
     * Find all appointments for a specific volunteer.
     */
    fun findByVolunteerId(volunteerId: Long): List<PsychooncologyAppointment>

    /**
     * Find all upcoming (scheduled) appointments ordered by scheduled time ascending.
     */
    fun findByStatusOrderByScheduledAtAsc(status: AppointmentStatus): List<PsychooncologyAppointment>
}
