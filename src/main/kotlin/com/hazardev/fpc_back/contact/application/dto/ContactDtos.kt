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
    val scheduledNextContactId: UUID? = null,
    val serviceReferral: ContactServiceReferralRequest? = null
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
    val scheduledNextContactId: UUID? = null,
    val serviceReferral: ContactServiceReferralRequest? = null
)

data class ContactServiceReferralRequest(
    val referredToSocialWorker: Boolean? = null,
    val referredToSusalud: Boolean? = null,
    val susaludRegistrationNumber: String? = null,
    val receivedFoodGuide: Boolean? = null,
    val participatesInGam: Boolean? = null,
    val programSatisfaction: String? = null,
    val wellbeingChanges: String? = null,
    val knowsAboutFissal: Boolean? = null,
    val referredToPaus: Boolean? = null,
    val referredToDae: Boolean? = null,
    val referredToFissal: Boolean? = null
)

data class ContactServiceReferralResponse(
    val id: UUID,
    val contactId: UUID,
    val referredToSocialWorker: Boolean?,
    val referredToSusalud: Boolean?,
    val susaludRegistrationNumber: String?,
    val receivedFoodGuide: Boolean?,
    val participatesInGam: Boolean?,
    val programSatisfaction: String?,
    val wellbeingChanges: String?,
    val knowsAboutFissal: Boolean?,
    val referredToPaus: Boolean?,
    val referredToDae: Boolean?,
    val referredToFissal: Boolean?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
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
    val serviceReferral: ContactServiceReferralResponse?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
