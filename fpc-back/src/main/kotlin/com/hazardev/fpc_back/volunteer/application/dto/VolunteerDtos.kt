package com.hazardev.fpc_back.volunteer.application.dto

import com.hazardev.fpc_back.shared.domain.AvailabilityStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

data class CreateVolunteerRequest(
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val specialty: String,
    val email: String,
    val phone: String,
    val isActive: Boolean = true
)

data class UpdateVolunteerRequest(
    val firstName: String? = null,
    val lastName: String? = null,
    val specialty: String? = null,
    val email: String? = null,
    val phone: String? = null,
    val isActive: Boolean? = null
)

data class VolunteerResponse(
    val id: UUID,
    val userId: UUID,
    val firstName: String,
    val lastName: String,
    val specialty: String,
    val email: String,
    val phone: String,
    val isActive: Boolean,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?
)

data class UpdateSlotRequest(
    val date: LocalDate? = null,
    val startTime: LocalTime? = null,
    val endTime: LocalTime? = null,
    val status: AvailabilityStatus? = null
)

/**
 * Request to create a new availability slot for a volunteer.
 * The frontend is expected to split date ranges into individual 1-hour slots
 * before sending.
 */
data class CreateSlotRequest(
    val volunteerId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)

data class AvailabilitySlotResponse(
    val id: UUID,
    val volunteerId: UUID,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: AvailabilityStatus
)
