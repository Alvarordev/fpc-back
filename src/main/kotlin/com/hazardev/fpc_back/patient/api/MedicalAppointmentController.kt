package com.hazardev.fpc_back.patient.api

import com.hazardev.fpc_back.patient.application.PatientService
import com.hazardev.fpc_back.patient.application.dto.CreateStandaloneAppointmentRequest
import com.hazardev.fpc_back.patient.application.dto.MedicalAppointmentResponse
import com.hazardev.fpc_back.patient.application.dto.UpdateMedicalAppointmentRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/medical-appointments")
class MedicalAppointmentController(
    private val patientService: PatientService
) {

    @GetMapping
    fun getAllMedicalAppointments(): List<MedicalAppointmentResponse> {
        return patientService.getAllMedicalAppointments()
    }

    @PostMapping
    fun createMedicalAppointment(
        @RequestBody request: CreateStandaloneAppointmentRequest
    ): ResponseEntity<MedicalAppointmentResponse> {
        val response = patientService.createStandaloneMedicalAppointment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun updateMedicalAppointment(
        @PathVariable id: UUID,
        @RequestBody request: UpdateMedicalAppointmentRequest
    ): ResponseEntity<MedicalAppointmentResponse> {
        val response = patientService.updateMedicalAppointment(id, request)
        return ResponseEntity.ok(response)
    }
}
