package com.hazardev.fpc_back.psychooncology.application

import com.hazardev.fpc_back.contact.infrastructure.ContactRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.psychooncology.application.dto.CompleteAppointmentRequest
import com.hazardev.fpc_back.psychooncology.application.dto.PsychooncologyAppointmentResponse
import com.hazardev.fpc_back.psychooncology.application.dto.ScheduleAppointmentRequest
import com.hazardev.fpc_back.psychooncology.domain.PsychooncologyAppointment
import com.hazardev.fpc_back.psychooncology.infrastructure.PsychooncologyAppointmentRepository
import com.hazardev.fpc_back.shared.domain.AppointmentStatus
import com.hazardev.fpc_back.volunteer.application.VolunteerAvailabilityService
import com.hazardev.fpc_back.volunteer.infrastructure.VolunteerAvailabilityRepository
import com.hazardev.fpc_back.volunteer.infrastructure.VolunteerRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Manages the lifecycle of psycho-oncology appointments.
 *
 * Coordinates between patients, volunteers, contacts, and availability slots.
 * Enforces business rules including the additional-session constraint and
 * atomic slot reservation via pessimistic locking.
 */
@Service
class PsychooncologyAppointmentService(
    private val appointmentRepository: PsychooncologyAppointmentRepository,
    private val patientRepository: PatientRepository,
    private val volunteerRepository: VolunteerRepository,
    private val contactRepository: ContactRepository,
    private val volunteerAvailabilityService: VolunteerAvailabilityService,
    private val volunteerAvailabilityRepository: VolunteerAvailabilityRepository
) {

    /**
     * Schedule a new psycho-oncology appointment.
     *
     * Business rules enforced:
     * 1. The availability slot must exist and be AVAILABLE
     * 2. isAdditionalSession can only be true when sessionNumber > 4
     * 3. The slot is atomically reserved to prevent double-booking
     *
     * Both the slot reservation and appointment creation run in the same transaction,
     * ensuring atomicity: either both succeed or neither does.
     *
     * @param request containing all required scheduling data
     * @return the created appointment as a response DTO
     * @throws IllegalArgumentException if isAdditionalSession is true but sessionNumber <= 4
     * @throws IllegalStateException if the availability slot is not AVAILABLE
     * @throws EntityNotFoundException if any referenced entity does not exist
     */
    @Transactional
    fun scheduleAppointment(request: ScheduleAppointmentRequest): PsychooncologyAppointmentResponse {
        // Validate business rule: additional sessions only after session 4
        if (request.isAdditionalSession && request.sessionNumber <= 4) {
            throw IllegalArgumentException(
                "isAdditionalSession can only be true when sessionNumber > 4. " +
                    "Received sessionNumber=${request.sessionNumber}"
            )
        }

        // Load referenced entities (throws EntityNotFoundException if any are missing)
        val patient = patientRepository.findById(request.patientId)
            .orElseThrow { EntityNotFoundException("Patient not found with id: ${request.patientId}") }

        val volunteer = volunteerRepository.findById(request.volunteerId)
            .orElseThrow { EntityNotFoundException("Volunteer not found with id: ${request.volunteerId}") }

        val contact = contactRepository.findById(request.contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with id: ${request.contactId}") }

        // Atomically reserve the slot using pessimistic locking.
        // This runs within the same transaction and holds the lock until commit.
        volunteerAvailabilityService.reserveSlot(request.availabilityId)

        // Reload the availability entity now that it's locked and reserved.
        // Since we're in the same transaction, the locked row is still held.
        val availability = volunteerAvailabilityRepository.findById(request.availabilityId)
            .orElseThrow { EntityNotFoundException("Availability slot not found with id: ${request.availabilityId}") }

        val appointment = PsychooncologyAppointment(
            patient = patient,
            volunteer = volunteer,
            contact = contact,
            availability = availability,
            patientEmail = request.patientEmail,
            sessionNumber = request.sessionNumber,
            isAdditionalSession = request.isAdditionalSession,
            modality = request.modality,
            status = AppointmentStatus.SCHEDULED,
            scheduledAt = request.scheduledAt
        )

        return appointmentRepository.save(appointment).toResponse()
    }

    /**
     * Complete an appointment by recording post-session data and setting the status to COMPLETED.
     *
     * This is called by the volunteer after the session has concluded.
     * Only appointments in SCHEDULED status can be completed.
     *
     * @param appointmentId the ID of the appointment to complete
     * @param request containing optional post-session fields (all nullable)
     * @return the updated appointment as a response DTO
     * @throws EntityNotFoundException if the appointment does not exist
     * @throws IllegalStateException if the appointment is not in SCHEDULED status
     */
    @Transactional
    fun completeAppointment(
        appointmentId: Long,
        request: CompleteAppointmentRequest
    ): PsychooncologyAppointmentResponse {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { EntityNotFoundException("Appointment not found with id: $appointmentId") }

        if (appointment.status != AppointmentStatus.SCHEDULED) {
            throw IllegalStateException(
                "Cannot complete appointment $appointmentId: current status is ${appointment.status}, expected SCHEDULED"
            )
        }

        appointment.apply {
            topicAddressed = request.topicAddressed
            sessionDetails = request.sessionDetails
            additionalObservations = request.additionalObservations
            recommendations = request.recommendations
            referral = request.referral
            status = AppointmentStatus.COMPLETED
            completedAt = LocalDateTime.now()
        }

        return appointmentRepository.save(appointment).toResponse()
    }

    /**
     * Cancel an appointment and release the reserved availability slot.
     *
     * Two-step operation within a single transaction:
     * 1. Set the appointment status to CANCELLED
     * 2. Release the associated availability slot back to AVAILABLE
     *
     * Only SCHEDULED appointments can be cancelled.
     *
     * @param appointmentId the ID of the appointment to cancel
     * @throws EntityNotFoundException if the appointment does not exist
     * @throws IllegalStateException if the appointment is not in SCHEDULED status
     */
    @Transactional
    fun cancelAppointment(appointmentId: Long) {
        val appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow { EntityNotFoundException("Appointment not found with id: $appointmentId") }

        if (appointment.status != AppointmentStatus.SCHEDULED) {
            throw IllegalStateException(
                "Cannot cancel appointment $appointmentId: current status is ${appointment.status}, expected SCHEDULED"
            )
        }

        val availabilityId = appointment.availability.id!!

        appointment.status = AppointmentStatus.CANCELLED
        appointmentRepository.save(appointment)

        // Release the availability slot so it can be reused
        volunteerAvailabilityService.releaseSlot(availabilityId)
    }

    /**
     * Get all appointments for a specific patient.
     *
     * @param patientId the patient's ID
     * @return list of appointments ordered by creation time (descending)
     */
    fun getAppointmentsByPatient(patientId: Long): List<PsychooncologyAppointmentResponse> {
        return appointmentRepository.findByPatientId(patientId)
            .map { it.toResponse() }
    }

    /**
     * Get all appointments for a specific volunteer.
     *
     * @param volunteerId the volunteer's ID
     * @return list of appointments ordered by creation time (descending)
     */
    fun getAppointmentsByVolunteer(volunteerId: Long): List<PsychooncologyAppointmentResponse> {
        return appointmentRepository.findByVolunteerId(volunteerId)
            .map { it.toResponse() }
    }

    /**
     * Get all upcoming (SCHEDULED) appointments ordered by scheduled time ascending.
     *
     * @return list of scheduled appointments, earliest first
     */
    fun getUpcomingAppointments(): List<PsychooncologyAppointmentResponse> {
        return appointmentRepository.findByStatusOrderByScheduledAtAsc(AppointmentStatus.SCHEDULED)
            .map { it.toResponse() }
    }

    /**
     * Map entity to response DTO.
     */
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
