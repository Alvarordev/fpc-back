package com.hazardev.fpc_back.psychooncology.api

import com.hazardev.fpc_back.psychooncology.application.PsychooncologyAppointmentService
import com.hazardev.fpc_back.psychooncology.application.dto.CompleteAppointmentRequest
import com.hazardev.fpc_back.psychooncology.application.dto.PsychooncologyAppointmentResponse
import com.hazardev.fpc_back.psychooncology.application.dto.ScheduleAppointmentRequest
import com.hazardev.fpc_back.psychooncology.application.dto.UpdateAppointmentRequest
import com.hazardev.fpc_back.psychooncology.domain.PsychooncologyAppointment
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

@RestController
@RequestMapping("/api/psychooncology-appointments")
class PsychooncologyAppointmentController(
    private val appointmentService: PsychooncologyAppointmentService
) {

    @GetMapping
    fun listAppointments(
        @RequestParam(required = false) patientId: Long?,
        @RequestParam(required = false) volunteerId: Long?,
        @RequestParam(required = false) upcoming: Boolean?
    ): List<PsychooncologyAppointmentResponse> {
        return when {
            patientId != null -> appointmentService.getAppointmentsByPatient(patientId)
            volunteerId != null -> appointmentService.getAppointmentsByVolunteer(volunteerId)
            upcoming == true -> appointmentService.getUpcomingAppointments()
            else -> appointmentService.listAll().map { it.toResponse() }
        }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: Long): PsychooncologyAppointmentResponse {
        return appointmentService.getById(id).toResponse()
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun scheduleAppointment(
        @RequestBody request: ScheduleAppointmentRequest
    ): ResponseEntity<PsychooncologyAppointmentResponse> {
        val response = appointmentService.scheduleAppointment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAppointment(
        @PathVariable id: Long,
        @RequestBody request: UpdateAppointmentRequest
    ): PsychooncologyAppointmentResponse {
        return appointmentService.updateAppointment(id, request).toResponse()
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAppointment(@PathVariable id: Long): ResponseEntity<Void> {
        appointmentService.deleteAppointment(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/complete")
    fun completeAppointment(
        @PathVariable id: Long,
        @RequestBody request: CompleteAppointmentRequest
    ): PsychooncologyAppointmentResponse {
        return appointmentService.completeAppointment(id, request)
    }

    @PostMapping("/{id}/cancel")
    fun cancelAppointment(@PathVariable id: Long): ResponseEntity<Void> {
        appointmentService.cancelAppointment(id)
        return ResponseEntity.ok().build()
    }

    private fun PsychooncologyAppointment.toResponse(): PsychooncologyAppointmentResponse =
        PsychooncologyAppointmentResponse(
            id = id!!,
            patientId = patient.id!!,
            volunteerId = volunteer.id!!,
            contactId = contact.id!!,
            availabilityId = availability.id!!,
            patientEmail = patientEmail,
            sessionNumber = sessionNumber,
            isAdditionalSession = isAdditionalSession,
            modality = modality,
            status = status,
            scheduledAt = scheduledAt,
            completedAt = completedAt,
            topicAddressed = topicAddressed,
            sessionDetails = sessionDetails,
            additionalObservations = additionalObservations,
            recommendations = recommendations,
            referral = referral,
            createdAt = createdAt!!,
            updatedAt = updatedAt!!
        )
}
