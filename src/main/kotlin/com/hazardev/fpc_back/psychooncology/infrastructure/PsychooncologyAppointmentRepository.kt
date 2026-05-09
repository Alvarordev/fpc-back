package com.hazardev.fpc_back.psychooncology.infrastructure

import com.hazardev.fpc_back.psychooncology.domain.PsychooncologyAppointment
import com.hazardev.fpc_back.shared.domain.AppointmentStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface PsychooncologyAppointmentRepository : JpaRepository<PsychooncologyAppointment, UUID> {

    fun findByPatientId(patientId: UUID): List<PsychooncologyAppointment>

    fun findByVolunteerId(volunteerId: UUID): List<PsychooncologyAppointment>

    fun findByStatusOrderByScheduledAtAsc(status: AppointmentStatus): List<PsychooncologyAppointment>
}
