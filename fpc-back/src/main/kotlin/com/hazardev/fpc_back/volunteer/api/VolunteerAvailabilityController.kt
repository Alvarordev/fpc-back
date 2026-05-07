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

@RestController
@RequestMapping("/api/volunteers/{volunteerId}/availability")
class VolunteerAvailabilityController(
    private val volunteerAvailabilityService: VolunteerAvailabilityService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createSlot(@RequestBody request: CreateSlotRequest): ResponseEntity<AvailabilitySlotResponse> {
        val response = volunteerAvailabilityService.createSlot(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getAllSlots(
        @PathVariable volunteerId: Long,
        @RequestParam(required = false) startDate: LocalDate?,
        @RequestParam(required = false) endDate: LocalDate?
    ): List<AvailabilitySlotResponse> {
        if (startDate != null && endDate != null) {
            return volunteerAvailabilityService.getAvailableSlots(volunteerId, startDate, endDate)
        }
        return volunteerAvailabilityService.getAllSlotsByVolunteer(volunteerId).map { slot ->
            AvailabilitySlotResponse(
                id = slot.id!!,
                volunteerId = slot.volunteer.id!!,
                date = slot.date,
                startTime = slot.startTime,
                endTime = slot.endTime,
                status = slot.status
            )
        }
    }

    @GetMapping("/{id}")
    fun getSlotById(
        @PathVariable volunteerId: Long,
        @PathVariable id: Long
    ): AvailabilitySlotResponse {
        val slot = volunteerAvailabilityService.getSlotById(id)
        return AvailabilitySlotResponse(
            id = slot.id!!,
            volunteerId = slot.volunteer.id!!,
            date = slot.date,
            startTime = slot.startTime,
            endTime = slot.endTime,
            status = slot.status
        )
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateSlot(
        @PathVariable volunteerId: Long,
        @PathVariable id: Long,
        @RequestBody request: UpdateSlotRequest
    ): AvailabilitySlotResponse {
        val slot = volunteerAvailabilityService.updateSlot(id, request)
        return AvailabilitySlotResponse(
            id = slot.id!!,
            volunteerId = slot.volunteer.id!!,
            date = slot.date,
            startTime = slot.startTime,
            endTime = slot.endTime,
            status = slot.status
        )
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteSlot(
        @PathVariable volunteerId: Long,
        @PathVariable id: Long
    ): ResponseEntity<Void> {
        volunteerAvailabilityService.deleteSlot(id)
        return ResponseEntity.noContent().build()
    }
}
