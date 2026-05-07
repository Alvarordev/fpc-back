package com.hazardev.fpc_back.volunteer.application

import com.hazardev.fpc_back.shared.domain.AvailabilityStatus
import com.hazardev.fpc_back.volunteer.application.dto.AvailabilitySlotResponse
import com.hazardev.fpc_back.volunteer.application.dto.CreateSlotRequest
import com.hazardev.fpc_back.volunteer.domain.Volunteer
import com.hazardev.fpc_back.volunteer.domain.VolunteerAvailability
import com.hazardev.fpc_back.volunteer.infrastructure.VolunteerAvailabilityRepository
import com.hazardev.fpc_back.volunteer.infrastructure.VolunteerRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

/**
 * Manages volunteer availability slots.
 *
 * Each slot represents a 1-hour block that can be reserved for appointments.
 * The frontend is expected to split date ranges into individual 1-hour slots
 * before sending creation requests.
 *
 * Concurrency safety for slot reservation uses pessimistic locking
 * (SELECT ... FOR UPDATE) to prevent double-booking race conditions.
 */
@Service
class VolunteerAvailabilityService(
    private val availabilityRepository: VolunteerAvailabilityRepository,
    private val volunteerRepository: VolunteerRepository
) {

    /**
     * Create a new availability slot for a volunteer.
     *
     * Validates that no duplicate slot exists (same volunteer, date, and start time).
     * The unique database constraint acts as a safety net, but the service-layer
     * check provides a clear error message before the database rejects it.
     *
     * @param request containing volunteerId, date, startTime, and endTime
     * @return the created availability slot as a response DTO
     * @throws EntityNotFoundException if the volunteer does not exist
     * @throws IllegalStateException if a duplicate slot already exists
     */
    @Transactional
    fun createSlot(request: CreateSlotRequest): AvailabilitySlotResponse {
        val volunteer = volunteerRepository.findById(request.volunteerId)
            .orElseThrow { EntityNotFoundException("Volunteer not found with id: ${request.volunteerId}") }

        if (availabilityRepository.existsByVolunteerIdAndDateAndStartTime(
                volunteerId = request.volunteerId,
                date = request.date,
                startTime = request.startTime
            )
        ) {
            throw IllegalStateException(
                "Slot already exists for volunteer ${request.volunteerId} " +
                    "on ${request.date} at ${request.startTime}"
            )
        }

        val slot = VolunteerAvailability(
            volunteer = volunteer,
            date = request.date,
            startTime = request.startTime,
            endTime = request.endTime,
            status = AvailabilityStatus.AVAILABLE
        )

        return availabilityRepository.save(slot).toResponse()
    }

    /**
     * Get all AVAILABLE slots for a volunteer within a date range.
     *
     * @param volunteerId the volunteer's ID
     * @param startDate inclusive start of the date range
     * @param endDate inclusive end of the date range
     * @return list of availability slots
     */
    fun getAvailableSlots(
        volunteerId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<AvailabilitySlotResponse> {
        return availabilityRepository.findByVolunteerIdAndDateBetween(
            volunteerId = volunteerId,
            startDate = startDate,
            endDate = endDate
        ).filter { it.status == AvailabilityStatus.AVAILABLE }
            .map { it.toResponse() }
    }

    /**
     * Reserve an availability slot by changing its status from AVAILABLE to RESERVED.
     *
     * Uses a pessimistic write lock (SELECT ... FOR UPDATE) to prevent race conditions
     * where two threads/appointments attempt to reserve the same slot simultaneously.
     *
     * @param slotId the availability slot ID to reserve
     * @return the reserved slot as a response DTO
     * @throws EntityNotFoundException if the slot does not exist
     * @throws IllegalStateException if the slot is not currently AVAILABLE
     */
    @Transactional
    fun reserveSlot(slotId: Long): AvailabilitySlotResponse {
        val slot = availabilityRepository.findByIdWithLock(slotId)
            .orElseThrow { EntityNotFoundException("Availability slot not found with id: $slotId") }

        if (slot.status != AvailabilityStatus.AVAILABLE) {
            throw IllegalStateException(
                "Cannot reserve slot $slotId: current status is ${slot.status}, expected AVAILABLE"
            )
        }

        slot.status = AvailabilityStatus.RESERVED
        return availabilityRepository.save(slot).toResponse()
    }

    /**
     * Release a reserved slot back to AVAILABLE.
     *
     * Typically called when an appointment is cancelled and the slot
     * should become available again for other appointments.
     *
     * @param slotId the availability slot ID to release
     * @return the released slot as a response DTO
     * @throws EntityNotFoundException if the slot does not exist
     * @throws IllegalStateException if the slot is not currently RESERVED
     */
    @Transactional
    fun releaseSlot(slotId: Long): AvailabilitySlotResponse {
        val slot = availabilityRepository.findById(slotId)
            .orElseThrow { EntityNotFoundException("Availability slot not found with id: $slotId") }

        if (slot.status != AvailabilityStatus.RESERVED) {
            throw IllegalStateException(
                "Cannot release slot $slotId: current status is ${slot.status}, expected RESERVED"
            )
        }

        slot.status = AvailabilityStatus.AVAILABLE
        return availabilityRepository.save(slot).toResponse()
    }

    /**
     * Map entity to response DTO.
     */
    private fun VolunteerAvailability.toResponse(): AvailabilitySlotResponse = AvailabilitySlotResponse(
        id = id!!,
        volunteerId = volunteer.id!!,
        date = date,
        startTime = startTime,
        endTime = endTime,
        status = status
    )
}
