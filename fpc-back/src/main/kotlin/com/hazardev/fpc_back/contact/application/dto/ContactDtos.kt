package com.hazardev.fpc_back.contact.application.dto

import com.hazardev.fpc_back.shared.domain.ContactPurpose
import com.hazardev.fpc_back.shared.domain.ContactStatus
import com.hazardev.fpc_back.shared.domain.ContactType
import java.time.LocalDateTime
import java.util.UUID

data class CreateContactRequest(
    val patientId: Long,
    val agentId: UUID? = null,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose,
    val scheduledAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val notes: String? = null,
    val scheduledNextContactId: Long? = null
)

data class UpdateContactRequest(
    val patientId: Long? = null,
    val agentId: UUID? = null,
    val type: ContactType? = null,
    val status: ContactStatus? = null,
    val purpose: ContactPurpose? = null,
    val scheduledAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val notes: String? = null,
    val scheduledNextContactId: Long? = null
)

data class ContactResponse(
    val id: Long,
    val patientId: Long,
    val agentId: UUID?,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose,
    val scheduledAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val notes: String?,
    val scheduledNextContactId: Long?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
