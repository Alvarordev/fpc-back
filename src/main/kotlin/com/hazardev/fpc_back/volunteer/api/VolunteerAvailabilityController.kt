package com.hazardev.fpc_back.volunteer.api

import com.hazardev.fpc_back.volunteer.application.VolunteerAvailabilityService
import com.hazardev.fpc_back.volunteer.application.dto.AvailabilitySlotResponse
import com.hazardev.fpc_back.volunteer.application.dto.CreateSlotRequest
import com.hazardev.fpc_back.volunteer.application.dto.UpdateSlotRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDate
import java.util.UUID

@RestController
@RequestMapping("/api/volunteers/{volunteerId}/availability")
class VolunteerAvailabilityController(
    private val volunteerAvailabilityService: VolunteerAvailabilityService
) {

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'VOLUNTEER')")
    fun createSlot(@RequestBody request: CreateSlotRequest): ResponseEntity<AvailabilitySlotResponse> {
        val response = volunteerAvailabilityService.createSlot(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getAllSlots(
        @PathVariable volunteerId: UUID,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?
    ): List<AvailabilitySlotResponse> {
        if (startDate != null && endDate != null) {
            return volunteerAvailabilityService.getAvailableSlots(volunteerId, startDate, endDate)
        }
        return volunteerAvailabilityService.getAllSlotsByVolunteer(volunteerId).map { slot ->
            AvailabilitySlotResponse(
                id = slot.id ?: throw IllegalStateException("Slot ID is null"),
                volunteerId = slot.volunteer.id ?: throw IllegalStateException("Volunteer ID is null on slot"),
                date = slot.date,
                startTime = slot.startTime,
                endTime = slot.endTime,
                status = slot.status
            )
        }
    }

    @GetMapping("/{id}")
    fun getSlotById(
        @PathVariable volunteerId: UUID,
        @PathVariable id: UUID
    ): AvailabilitySlotResponse {
        val slot = volunteerAvailabilityService.getSlotById(id)
        return AvailabilitySlotResponse(
            id = slot.id ?: throw IllegalStateException("Slot ID is null"),
            volunteerId = slot.volunteer.id ?: throw IllegalStateException("Volunteer ID is null on slot"),
            date = slot.date,
            startTime = slot.startTime,
            endTime = slot.endTime,
            status = slot.status
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'VOLUNTEER')")
    fun updateSlot(
        @PathVariable volunteerId: UUID,
        @PathVariable id: UUID,
        @RequestBody request: UpdateSlotRequest
    ): AvailabilitySlotResponse {
        val slot = volunteerAvailabilityService.updateSlot(id, request)
        return AvailabilitySlotResponse(
            id = slot.id ?: throw IllegalStateException("Slot ID is null after update"),
            volunteerId = slot.volunteer.id ?: throw IllegalStateException("Volunteer ID is null on slot"),
            date = slot.date,
            startTime = slot.startTime,
            endTime = slot.endTime,
            status = slot.status
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'VOLUNTEER')")
    fun deleteSlot(
        @PathVariable volunteerId: UUID,
        @PathVariable id: UUID
    ): ResponseEntity<Void> {
        volunteerAvailabilityService.deleteSlot(id)
        return ResponseEntity.noContent().build()
    }
}
