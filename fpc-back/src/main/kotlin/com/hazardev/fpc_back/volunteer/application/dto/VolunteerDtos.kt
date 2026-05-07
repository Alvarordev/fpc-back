package com.hazardev.fpc_back.volunteer.application.dto

import com.hazardev.fpc_back.shared.domain.AvailabilityStatus
import java.time.LocalDate
import java.time.LocalTime

/**
 * Request to create a new availability slot for a volunteer.
 * The frontend is expected to split date ranges into individual 1-hour slots
 * before sending.
 */
data class CreateSlotRequest(
    val volunteerId: Long,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime
)

/**
 * Response containing details of an availability slot.
 */
data class AvailabilitySlotResponse(
    val id: Long,
    val volunteerId: Long,
    val date: LocalDate,
    val startTime: LocalTime,
    val endTime: LocalTime,
    val status: AvailabilityStatus
)
