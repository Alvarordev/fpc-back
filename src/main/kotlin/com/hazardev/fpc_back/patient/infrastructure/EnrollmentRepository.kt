package com.hazardev.fpc_back.patient.infrastructure

import com.hazardev.fpc_back.patient.domain.Enrollment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface EnrollmentRepository : JpaRepository<Enrollment, UUID> {

    fun findByPatientId(patientId: UUID): List<Enrollment>

    fun findByContactId(contactId: UUID): List<Enrollment>
}
