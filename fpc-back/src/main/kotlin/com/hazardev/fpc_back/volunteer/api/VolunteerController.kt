package com.hazardev.fpc_back.volunteer.api

import com.hazardev.fpc_back.volunteer.application.VolunteerService
import com.hazardev.fpc_back.volunteer.application.dto.CreateVolunteerRequest
import com.hazardev.fpc_back.volunteer.application.dto.UpdateVolunteerRequest
import com.hazardev.fpc_back.volunteer.application.dto.VolunteerResponse
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
import java.util.UUID

@RestController
@RequestMapping("/api/volunteers")
class VolunteerController(
    private val volunteerService: VolunteerService
) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun create(@RequestBody request: CreateVolunteerRequest): ResponseEntity<VolunteerResponse> {
        val response = volunteerService.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getAll(): List<VolunteerResponse> {
        return volunteerService.getAll()
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): VolunteerResponse {
        return volunteerService.getById(id)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun update(
        @PathVariable id: UUID,
        @RequestBody request: UpdateVolunteerRequest
    ): VolunteerResponse {
        return volunteerService.update(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun delete(@PathVariable id: UUID): ResponseEntity<Void> {
        volunteerService.delete(id)
        return ResponseEntity.noContent().build()
    }
}
