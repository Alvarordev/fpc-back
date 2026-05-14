package com.hazardev.fpc_back.volunteer.application

import com.hazardev.fpc_back.shared.domain.AvailabilityStatus
import com.hazardev.fpc_back.volunteer.application.dto.AvailabilitySlotResponse
import com.hazardev.fpc_back.volunteer.application.dto.CreateSlotRequest
import com.hazardev.fpc_back.volunteer.application.dto.UpdateSlotRequest
import com.hazardev.fpc_back.volunteer.domain.Volunteer
import com.hazardev.fpc_back.volunteer.domain.VolunteerAvailability
import com.hazardev.fpc_back.volunteer.infrastructure.VolunteerAvailabilityRepository
import com.hazardev.fpc_back.volunteer.infrastructure.VolunteerRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.UUID

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

        return availabilityRepository.saveAndFlush(slot).toResponse()
    }

    fun getAvailableSlots(
        volunteerId: UUID,
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
    fun reserveSlot(slotId: UUID): AvailabilitySlotResponse {
        val slot = availabilityRepository.findByIdWithLock(slotId)
            .orElseThrow { EntityNotFoundException("Availability slot not found with id: $slotId") }

        if (slot.status != AvailabilityStatus.AVAILABLE) {
            throw IllegalStateException(
                "Cannot reserve slot $slotId: current status is ${slot.status}, expected AVAILABLE"
            )
        }

        slot.status = AvailabilityStatus.RESERVED
        return availabilityRepository.saveAndFlush(slot).toResponse()
    }

    fun releaseSlot(slotId: UUID): AvailabilitySlotResponse {
        val slot = availabilityRepository.findById(slotId)
            .orElseThrow { EntityNotFoundException("Availability slot not found with id: $slotId") }

        if (slot.status != AvailabilityStatus.RESERVED) {
            throw IllegalStateException(
                "Cannot release slot $slotId: current status is ${slot.status}, expected RESERVED"
            )
        }

        slot.status = AvailabilityStatus.AVAILABLE
        return availabilityRepository.saveAndFlush(slot).toResponse()
    }

    fun getSlotById(id: UUID): VolunteerAvailability {
        return availabilityRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Availability slot not found with id: $id") }
    }

    fun getAllSlotsByVolunteer(volunteerId: UUID): List<VolunteerAvailability> {
        return availabilityRepository.findByVolunteerId(volunteerId)
    }

    @Transactional
    fun updateSlot(id: UUID, request: UpdateSlotRequest): VolunteerAvailability {
        val slot = getSlotById(id)
        request.date?.let { slot.date = it }
        request.startTime?.let { slot.startTime = it }
        request.endTime?.let { slot.endTime = it }
        request.status?.let { slot.status = it }
        return availabilityRepository.save(slot)
    }

    @Transactional
    fun deleteSlot(id: UUID) {
        val slot = getSlotById(id)
        availabilityRepository.delete(slot)
    }

    private fun VolunteerAvailability.toResponse(): AvailabilitySlotResponse = AvailabilitySlotResponse(
        id = id ?: throw IllegalStateException("Availability slot ID is null after save"),
        volunteerId = volunteer.id ?: throw IllegalStateException("Volunteer ID is null on availability slot"),
        date = date,
        startTime = startTime,
        endTime = endTime,
        status = status
    )
}
