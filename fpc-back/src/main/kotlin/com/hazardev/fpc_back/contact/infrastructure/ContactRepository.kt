package com.hazardev.fpc_back.contact.infrastructure

import com.hazardev.fpc_back.contact.domain.Contact
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface ContactRepository : JpaRepository<Contact, UUID> {

    fun findByPatientId(patientId: UUID): List<Contact>

    fun findByPatientIdOrderByCreatedAtDesc(patientId: UUID): List<Contact>

    fun findByAgentId(agentId: UUID): List<Contact>
}
