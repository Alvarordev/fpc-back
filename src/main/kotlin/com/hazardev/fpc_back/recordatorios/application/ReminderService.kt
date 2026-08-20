package com.hazardev.fpc_back.recordatorios.application

import com.hazardev.fpc_back.contact.infrastructure.ContactRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.recordatorios.application.dto.CreateReminderRequest
import com.hazardev.fpc_back.recordatorios.application.dto.ReminderResponse
import com.hazardev.fpc_back.recordatorios.application.dto.UpdateReminderRequest
import com.hazardev.fpc_back.recordatorios.domain.Reminder
import com.hazardev.fpc_back.recordatorios.infrastructure.ReminderRepository
import com.hazardev.fpc_back.shared.domain.ReminderStatus
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ReminderService(
    private val reminderRepository: ReminderRepository,
    private val patientRepository: PatientRepository,
    private val contactRepository: ContactRepository
) {

    /**
     * Get all reminders for a specific patient, ordered by scheduled date ascending.
     *
     * @param patientId the patient's ID
     * @return list of reminders as response DTOs
     */
    fun getRemindersByPatient(patientId: UUID): List<ReminderResponse> {
        return reminderRepository.findByPatientIdOrderByScheduledDateAsc(patientId)
            .map { it.toResponse() }
    }

    /**
     * Create a new reminder.
     *
     * Validates that the referenced patient and contact exist before creating.
     *
     * @param request the creation request
     * @return the created reminder as a response DTO
     * @throws EntityNotFoundException if patient or contact does not exist
     */
    @Transactional
    fun createReminder(request: CreateReminderRequest): ReminderResponse {
        val patient = patientRepository.findById(request.patientId)
            .orElseThrow { EntityNotFoundException("Patient not found with id: ${request.patientId}") }

        val contact = contactRepository.findById(request.contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with id: ${request.contactId}") }

        val reminder = Reminder(
            patient = patient,
            contact = contact,
            type = request.type,
            description = request.description,
            scheduledDate = request.scheduledDate,
            notes = request.notes,
            status = ReminderStatus.PENDIENTE
        )

        return reminderRepository.saveAndFlush(reminder).toResponse()
    }

    /**
     * Update an existing reminder's mutable fields.
     *
     * Only non-null fields in the request will be applied.
     *
     * @param id the reminder ID
     * @param request containing the fields to update (all optional)
     * @return the updated reminder as a response DTO
     * @throws EntityNotFoundException if the reminder does not exist
     */
    @Transactional
    fun updateReminder(id: UUID, request: UpdateReminderRequest): ReminderResponse {
        val reminder = getById(id)

        request.type?.let { reminder.type = it }
        request.description?.let { reminder.description = it }
        request.scheduledDate?.let { reminder.scheduledDate = it }
        request.notes?.let { reminder.notes = it }

        return reminderRepository.saveAndFlush(reminder).toResponse()
    }

    /**
     * Delete a reminder.
     *
     * @param id the reminder ID
     * @throws EntityNotFoundException if the reminder does not exist
     */
    @Transactional
    fun deleteReminder(id: UUID) {
        val reminder = getById(id)
        reminderRepository.delete(reminder)
    }

    /**
     * Mark a reminder as COMPLETADO.
     *
     * Only PENDIENTE reminders can be completed.
     *
     * @param id the reminder ID
     * @return the updated reminder as a response DTO
     * @throws EntityNotFoundException if the reminder does not exist
     * @throws IllegalStateException if the reminder is already COMPLETADO or CANCELADO
     */
    @Transactional
    fun completeReminder(id: UUID): ReminderResponse {
        val reminder = getById(id)

        if (reminder.status != ReminderStatus.PENDIENTE) {
            throw IllegalStateException(
                "Cannot complete reminder $id: current status is ${reminder.status}, expected PENDIENTE"
            )
        }

        reminder.status = ReminderStatus.COMPLETADO
        return reminderRepository.saveAndFlush(reminder).toResponse()
    }

    /**
     * Cancel a reminder.
     *
     * Only PENDIENTE reminders can be cancelled.
     *
     * @param id the reminder ID
     * @return the updated reminder as a response DTO
     * @throws EntityNotFoundException if the reminder does not exist
     * @throws IllegalStateException if the reminder is already COMPLETADO or CANCELADO
     */
    @Transactional
    fun cancelReminder(id: UUID): ReminderResponse {
        val reminder = getById(id)

        if (reminder.status != ReminderStatus.PENDIENTE) {
            throw IllegalStateException(
                "Cannot cancel reminder $id: current status is ${reminder.status}, expected PENDIENTE"
            )
        }

        reminder.status = ReminderStatus.CANCELADO
        return reminderRepository.saveAndFlush(reminder).toResponse()
    }

    /**
     * Get a single reminder by ID.
     *
     * @param id the reminder ID
     * @return the reminder entity
     * @throws EntityNotFoundException if the reminder does not exist
     */
    fun getById(id: UUID): Reminder {
        return reminderRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Reminder not found with id: $id") }
    }

    private fun Reminder.toResponse(): ReminderResponse =
        ReminderResponse(
            id = id ?: throw IllegalStateException("Reminder ID is null after save"),
            patientId = patient.id ?: throw IllegalStateException("Patient ID is null on reminder"),
            contactId = contact.id ?: throw IllegalStateException("Contact ID is null on reminder"),
            type = type,
            description = description,
            scheduledDate = scheduledDate,
            status = status,
            notes = notes,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
}
