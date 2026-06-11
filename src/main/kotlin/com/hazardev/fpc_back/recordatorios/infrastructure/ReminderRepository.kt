package com.hazardev.fpc_back.recordatorios.infrastructure

import com.hazardev.fpc_back.recordatorios.domain.Reminder
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ReminderRepository : JpaRepository<Reminder, UUID> {

    fun findByPatientIdOrderByScheduledDateAsc(patientId: UUID): List<Reminder>
}
