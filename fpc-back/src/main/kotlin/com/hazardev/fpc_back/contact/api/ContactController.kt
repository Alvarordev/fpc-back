package com.hazardev.fpc_back.contact.api

import com.hazardev.fpc_back.contact.application.ContactService
import com.hazardev.fpc_back.contact.application.dto.ContactResponse
import com.hazardev.fpc_back.contact.application.dto.CreateContactRequest
import com.hazardev.fpc_back.contact.application.dto.UpdateContactRequest
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
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/contacts")
class ContactController(
    private val contactService: ContactService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createContact(@RequestBody request: CreateContactRequest): ResponseEntity<ContactResponse> {
        val response = contactService.createContact(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun listContacts(): List<ContactResponse> {
        return contactService.listContacts()
    }

    @GetMapping("/{id}")
    fun getContactById(@PathVariable id: Long): ContactResponse {
        return contactService.getContactById(id)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateContact(
        @PathVariable id: Long,
        @RequestBody request: UpdateContactRequest
    ): ContactResponse {
        return contactService.updateContact(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteContact(@PathVariable id: Long): ResponseEntity<Void> {
        contactService.deleteContact(id)
        return ResponseEntity.noContent().build()
    }
}
