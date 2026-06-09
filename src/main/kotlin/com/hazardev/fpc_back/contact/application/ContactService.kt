package com.hazardev.fpc_back.contact.application

import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
import com.hazardev.fpc_back.contact.application.dto.ContactServiceReferralRequest
import com.hazardev.fpc_back.contact.application.dto.ContactServiceReferralResponse
import com.hazardev.fpc_back.contact.application.dto.ContactResponse
import com.hazardev.fpc_back.contact.application.dto.CreateContactRequest
import com.hazardev.fpc_back.contact.application.dto.UpdateContactRequest
import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.contact.domain.ContactServiceReferral
import com.hazardev.fpc_back.contact.infrastructure.ContactRepository
import com.hazardev.fpc_back.contact.infrastructure.ContactServiceReferralRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class ContactService(
    private val contactRepository: ContactRepository,
    private val contactServiceReferralRepository: ContactServiceReferralRepository,
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

        val savedContact = contactRepository.saveAndFlush(contact)
        val serviceReferral = upsertServiceReferral(savedContact, request.serviceReferral)
        return savedContact.toResponse(serviceReferral)
    }

    fun getContactById(id: UUID): ContactResponse {
        val contact = contactRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Contact not found with id: $id") }
        return contact.toResponse(contactServiceReferralRepository.findByContactId(id))
    }

    fun listContacts(): List<ContactResponse> {
        val contacts = contactRepository.findAll()
        val serviceReferrals = contactServiceReferralRepository.findByContactIdIn(contacts.mapNotNull { it.id })
            .associateBy { it.contact.id!! }
        return contacts.map { contact ->
            contact.toResponse(contact.id?.let(serviceReferrals::get))
        }
    }

    @Transactional
    fun updateContact(id: UUID, request: UpdateContactRequest): ContactResponse {
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

        val savedContact = contactRepository.saveAndFlush(contact)
        val serviceReferral = if (request.serviceReferral != null) {
            upsertServiceReferral(savedContact, request.serviceReferral)
        } else {
            contactServiceReferralRepository.findByContactId(savedContact.id!!)
        }

        return savedContact.toResponse(serviceReferral)
    }

    @Transactional
    fun deleteContact(id: UUID) {
        if (!contactRepository.existsById(id)) {
            throw EntityNotFoundException("Contact not found with id: $id")
        }
        contactRepository.deleteById(id)
    }

    private fun upsertServiceReferral(
        contact: Contact,
        request: ContactServiceReferralRequest?
    ): ContactServiceReferral? {
        if (request == null || !request.hasAnyValue()) {
            return contact.id?.let { contactServiceReferralRepository.findByContactId(it) }
        }

        val existing = contact.id?.let { contactServiceReferralRepository.findByContactId(it) }
        val serviceReferral = existing ?: ContactServiceReferral(contact = contact)

        request.referredToSocialWorker?.let { serviceReferral.referredToSocialWorker = it }
        request.referredToSusalud?.let { serviceReferral.referredToSusalud = it }
        request.susaludRegistrationNumber?.let { serviceReferral.susaludRegistrationNumber = it }
        request.receivedFoodGuide?.let { serviceReferral.receivedFoodGuide = it }
        request.participatesInGam?.let { serviceReferral.participatesInGam = it }
        request.programSatisfaction?.let { serviceReferral.programSatisfaction = it }
        request.wellbeingChanges?.let { serviceReferral.wellbeingChanges = it }
        request.knowsAboutFissal?.let { serviceReferral.knowsAboutFissal = it }
        request.referredToPaus?.let { serviceReferral.referredToPaus = it }
        request.referredToDae?.let { serviceReferral.referredToDae = it }
        request.referredToFissal?.let { serviceReferral.referredToFissal = it }

        return contactServiceReferralRepository.saveAndFlush(serviceReferral)
    }

    private fun ContactServiceReferralRequest.hasAnyValue(): Boolean =
        referredToSocialWorker != null ||
            referredToSusalud != null ||
            susaludRegistrationNumber != null ||
            receivedFoodGuide != null ||
            participatesInGam != null ||
            programSatisfaction != null ||
            wellbeingChanges != null ||
            knowsAboutFissal != null ||
            referredToPaus != null ||
            referredToDae != null ||
            referredToFissal != null

    private fun Contact.toResponse(serviceReferral: ContactServiceReferral?): ContactResponse = ContactResponse(
        id = id ?: throw IllegalStateException("Contact ID is null after save"),
        patientId = patient.id ?: throw IllegalStateException("Patient ID is null on contact"),
        agentId = agent?.id,
        type = type,
        status = status,
        purpose = purpose,
        scheduledAt = scheduledAt,
        completedAt = completedAt,
        notes = notes,
        scheduledNextContactId = scheduledNextContact?.id,
        serviceReferral = serviceReferral?.toResponse(),
        createdAt = createdAt ?: throw IllegalStateException("createdAt is null on contact"),
        updatedAt = updatedAt ?: throw IllegalStateException("updatedAt is null on contact")
    )

    private fun ContactServiceReferral.toResponse(): ContactServiceReferralResponse = ContactServiceReferralResponse(
        id = id ?: throw IllegalStateException("Service referral ID is null after save"),
        contactId = contact.id ?: throw IllegalStateException("Contact ID is null on service referral"),
        referredToSocialWorker = referredToSocialWorker,
        referredToSusalud = referredToSusalud,
        susaludRegistrationNumber = susaludRegistrationNumber,
        receivedFoodGuide = receivedFoodGuide,
        participatesInGam = participatesInGam,
        programSatisfaction = programSatisfaction,
        wellbeingChanges = wellbeingChanges,
        knowsAboutFissal = knowsAboutFissal,
        referredToPaus = referredToPaus,
        referredToDae = referredToDae,
        referredToFissal = referredToFissal,
        createdAt = createdAt ?: throw IllegalStateException("createdAt is null on service referral"),
        updatedAt = updatedAt ?: throw IllegalStateException("updatedAt is null on service referral")
    )
}
