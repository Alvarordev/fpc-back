package com.hazardev.fpc_back.contact.api

import com.hazardev.fpc_back.contact.application.ContactService
import com.hazardev.fpc_back.contact.application.dto.ContactResponse
import com.hazardev.fpc_back.contact.application.dto.CreateContactRequest
import com.hazardev.fpc_back.contact.application.dto.UpdateContactRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/contacts")
class ContactController(
    private val contactService: ContactService
) {

    @PostMapping
    fun createContact(@RequestBody request: CreateContactRequest): ResponseEntity<ContactResponse> {
        val response = contactService.createContact(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun listContacts(): List<ContactResponse> {
        return contactService.listContacts()
    }

    @GetMapping("/{id}")
    fun getContactById(@PathVariable id: UUID): ContactResponse {
        return contactService.getContactById(id)
    }

    @PutMapping("/{id}")
    fun updateContact(
        @PathVariable id: UUID,
        @RequestBody request: UpdateContactRequest
    ): ContactResponse {
        return contactService.updateContact(id, request)
    }

    @DeleteMapping("/{id}")
    fun deleteContact(@PathVariable id: UUID): ResponseEntity<Void> {
        contactService.deleteContact(id)
        return ResponseEntity.noContent().build()
    }
}
