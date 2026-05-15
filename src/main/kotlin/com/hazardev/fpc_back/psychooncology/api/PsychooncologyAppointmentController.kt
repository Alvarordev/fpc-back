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
import java.util.UUID

@RestController
@RequestMapping("/api/psychooncology-appointments")
class PsychooncologyAppointmentController(
    private val appointmentService: PsychooncologyAppointmentService
) {

    @GetMapping
    fun listAppointments(
        @RequestParam(required = false) patientId: UUID?,
        @RequestParam(required = false) volunteerId: UUID?,
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
    fun getById(@PathVariable id: UUID): PsychooncologyAppointmentResponse {
        return appointmentService.getById(id).toResponse()
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'VOLUNTEER')")
    fun scheduleAppointment(
        @RequestBody request: ScheduleAppointmentRequest
    ): ResponseEntity<PsychooncologyAppointmentResponse> {
        val response = appointmentService.scheduleAppointment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'VOLUNTEER')")
    fun updateAppointment(
        @PathVariable id: UUID,
        @RequestBody request: UpdateAppointmentRequest
    ): PsychooncologyAppointmentResponse {
        return appointmentService.updateAppointment(id, request).toResponse()
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT', 'VOLUNTEER')")
    fun deleteAppointment(@PathVariable id: UUID): ResponseEntity<Void> {
        appointmentService.deleteAppointment(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/complete")
    fun completeAppointment(
        @PathVariable id: UUID,
        @RequestBody request: CompleteAppointmentRequest
    ): PsychooncologyAppointmentResponse {
        return appointmentService.completeAppointment(id, request)
    }

    @PostMapping("/{id}/cancel")
    fun cancelAppointment(@PathVariable id: UUID): ResponseEntity<Void> {
        appointmentService.cancelAppointment(id)
        return ResponseEntity.ok().build()
    }

    private fun PsychooncologyAppointment.toResponse(): PsychooncologyAppointmentResponse =
        PsychooncologyAppointmentResponse(
            id = id ?: throw IllegalStateException("Appointment ID is null"),
            patientId = patient.id ?: throw IllegalStateException("Patient ID is null on appointment"),
            volunteerId = volunteer.id ?: throw IllegalStateException("Volunteer ID is null on appointment"),
            contactId = contact.id ?: throw IllegalStateException("Contact ID is null on appointment"),
            availabilityId = availability.id ?: throw IllegalStateException("Availability ID is null on appointment"),
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
            createdAt = createdAt ?: throw IllegalStateException("createdAt is null on appointment"),
            updatedAt = updatedAt ?: throw IllegalStateException("updatedAt is null on appointment")
        )
}
