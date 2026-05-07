package com.hazardev.fpc_back.contact.application

import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
import com.hazardev.fpc_back.contact.application.dto.ContactResponse
import com.hazardev.fpc_back.contact.application.dto.CreateContactRequest
import com.hazardev.fpc_back.contact.application.dto.UpdateContactRequest
import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.contact.infrastructure.ContactRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ContactService(
    private val contactRepository: ContactRepository,
    private val patientRepository: PatientRepository,
    private val agentRepository: AgentRepository
) {

    @Transactional
    fun createContact(request: CreateContactRequest): ContactResponse {
        val patient = patientRepository.findById(request.patientId)
            .orElseThrow { EntityNotFoundException("Patient not found with id: ${request.patientId}") }

        val agent = request.agentId?.let { agentId ->
            agentRepository.findById(agentId)
                .orElseThrow { EntityNotFoundException("Agent not found with id: $agentId") }
        }

        val scheduledNextContact = request.scheduledNextContactId?.let { nextId ->
            contactRepository.findById(nextId)
                .orElseThrow { EntityNotFoundException("Contact not found with id: $nextId") }
        }

        val contact = Contact(
            patient = patient,
            agent = agent,
            type = request.type,
            status = request.status,
            purpose = request.purpose,
            scheduledAt = request.scheduledAt,
            completedAt = request.completedAt,
            notes = request.notes,
            scheduledNextContact = scheduledNextContact
        )

        return contactRepository.save(contact).toResponse()
    }

    fun getContactById(id: Long): ContactResponse {
        val contact = contactRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Contact not found with id: $id") }
        return contact.toResponse()
    }

    fun listContacts(): List<ContactResponse> {
        return contactRepository.findAll().map { it.toResponse() }
    }

    @Transactional
    fun updateContact(id: Long, request: UpdateContactRequest): ContactResponse {
        val contact = contactRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Contact not found with id: $id") }

        request.patientId?.let { newPatientId ->
            if (newPatientId != contact.patient.id) {
                val patient = patientRepository.findById(newPatientId)
                    .orElseThrow { EntityNotFoundException("Patient not found with id: $newPatientId") }
                contact.patient = patient
            }
        }

        request.agentId?.let { newAgentId ->
            if (newAgentId != contact.agent?.id) {
                val agent = agentRepository.findById(newAgentId)
                    .orElseThrow { EntityNotFoundException("Agent not found with id: $newAgentId") }
                contact.agent = agent
            }
        }

        request.type?.let { contact.type = it }
        request.status?.let { contact.status = it }
        request.purpose?.let { contact.purpose = it }
        request.scheduledAt?.let { contact.scheduledAt = it }
        request.completedAt?.let { contact.completedAt = it }
        request.notes?.let { contact.notes = it }
        request.scheduledNextContactId?.let { newNextId ->
            val nextContact = contactRepository.findById(newNextId)
                .orElseThrow { EntityNotFoundException("Contact not found with id: $newNextId") }
            contact.scheduledNextContact = nextContact
        }

        return contactRepository.save(contact).toResponse()
    }

    @Transactional
    fun deleteContact(id: Long) {
        if (!contactRepository.existsById(id)) {
            throw EntityNotFoundException("Contact not found with id: $id")
        }
        contactRepository.deleteById(id)
    }

    private fun Contact.toResponse(): ContactResponse = ContactResponse(
        id = id!!,
        patientId = patient.id!!,
        agentId = agent?.id,
        type = type,
        status = status,
        purpose = purpose,
        scheduledAt = scheduledAt,
        completedAt = completedAt,
        notes = notes,
        scheduledNextContactId = scheduledNextContact?.id,
        createdAt = createdAt!!,
        updatedAt = updatedAt!!
    )
}
