package com.hazardev.fpc_back.psychooncology.application.dto

import com.hazardev.fpc_back.shared.domain.AppointmentModality
import com.hazardev.fpc_back.shared.domain.AppointmentStatus
import com.hazardev.fpc_back.shared.domain.ReferralType
import java.time.LocalDateTime
import java.util.UUID

data class ScheduleAppointmentRequest(
    val patientId: UUID,
    val volunteerId: UUID,
    val contactId: UUID,
    val availabilityId: UUID,
    val patientEmail: String? = null,
    val sessionNumber: Int,
    val isAdditionalSession: Boolean = false,
    val modality: AppointmentModality,
    val scheduledAt: LocalDateTime
)

/**
 * Request to complete an appointment with post-session data filled by the volunteer.
 * All fields are optional — at least one should be provided, but the service
 * will accept any combination.
 */
data class CompleteAppointmentRequest(
    val topicAddressed: String? = null,
    val sessionDetails: String? = null,
    val additionalObservations: String? = null,
    val recommendations: String? = null,
    val referral: ReferralType? = null
)

data class UpdateAppointmentRequest(
    val patientId: UUID? = null,
    val volunteerId: UUID? = null,
    val contactId: UUID? = null,
    val availabilityId: UUID? = null,
    val patientEmail: String? = null,
    val sessionNumber: Int? = null,
    val isAdditionalSession: Boolean? = null,
    val modality: AppointmentModality? = null,
    val status: AppointmentStatus? = null,
    val scheduledAt: LocalDateTime? = null,
    val completedAt: LocalDateTime? = null,
    val topicAddressed: String? = null,
    val sessionDetails: String? = null,
    val additionalObservations: String? = null,
    val recommendations: String? = null,
    val referral: ReferralType? = null
)

data class PsychooncologyAppointmentResponse(
    val id: UUID,
    val patientId: UUID,
    val volunteerId: UUID,
    val contactId: UUID,
    val availabilityId: UUID,
    val patientEmail: String?,
    val sessionNumber: Int,
    val isAdditionalSession: Boolean,
    val modality: AppointmentModality,
    val status: AppointmentStatus,
    val scheduledAt: LocalDateTime,
    val completedAt: LocalDateTime?,
    val topicAddressed: String?,
    val sessionDetails: String?,
    val additionalObservations: String?,
    val recommendations: String?,
    val referral: ReferralType?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
