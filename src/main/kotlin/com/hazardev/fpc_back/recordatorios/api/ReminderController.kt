package com.hazardev.fpc_back.recordatorios.api

import com.hazardev.fpc_back.recordatorios.application.ReminderService
import com.hazardev.fpc_back.recordatorios.application.dto.CreateReminderRequest
import com.hazardev.fpc_back.recordatorios.application.dto.ReminderResponse
import com.hazardev.fpc_back.recordatorios.application.dto.UpdateReminderRequest
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
@RequestMapping("/api/recordatorios")
class ReminderController(
    private val reminderService: ReminderService
) {

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    fun getRemindersByPatient(
        @RequestParam patientId: UUID
    ): List<ReminderResponse> {
        return reminderService.getRemindersByPatient(patientId)
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    fun createReminder(
        @RequestBody request: CreateReminderRequest
    ): ResponseEntity<ReminderResponse> {
        val response = reminderService.createReminder(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    fun updateReminder(
        @PathVariable id: UUID,
        @RequestBody request: UpdateReminderRequest
    ): ReminderResponse {
        return reminderService.updateReminder(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    fun deleteReminder(@PathVariable id: UUID): ResponseEntity<Void> {
        reminderService.deleteReminder(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    fun completeReminder(@PathVariable id: UUID): ReminderResponse {
        return reminderService.completeReminder(id)
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'AGENT')")
    fun cancelReminder(@PathVariable id: UUID): ReminderResponse {
        return reminderService.cancelReminder(id)
    }
}
