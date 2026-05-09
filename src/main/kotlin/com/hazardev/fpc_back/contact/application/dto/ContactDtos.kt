package com.hazardev.fpc_back.contact.application.dto

import com.hazardev.fpc_back.shared.domain.ContactPurpose
import com.hazardev.fpc_back.shared.domain.ContactStatus
import com.hazardev.fpc_back.shared.domain.ContactType
import java.time.LocalDateTime
import java.util.UUID

data class CreateContactRequest(
    val patientId: UUID,
    val agentId: UUID? = null,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose,
    val scheduledAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val notes: String? = null,
    val scheduledNextContactId: UUID? = null
)

data class UpdateContactRequest(
    val patientId: UUID? = null,
    val agentId: UUID? = null,
    val type: ContactType? = null,
    val status: ContactStatus? = null,
    val purpose: ContactPurpose? = null,
    val scheduledAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val notes: String? = null,
    val scheduledNextContactId: UUID? = null
)

data class ContactResponse(
    val id: UUID,
    val patientId: UUID,
    val agentId: UUID?,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose,
    val scheduledAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val notes: String?,
    val scheduledNextContactId: UUID?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
