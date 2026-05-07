package com.hazardev.fpc_back.patient.api

import com.hazardev.fpc_back.patient.application.PatientService
import com.hazardev.fpc_back.patient.application.dto.AddDiagnosisRequest
import com.hazardev.fpc_back.patient.application.dto.AddInsuranceRequest
import com.hazardev.fpc_back.patient.application.dto.AddMedicalAppointmentRequest
import com.hazardev.fpc_back.patient.application.dto.AddSisAffiliationRequest
import com.hazardev.fpc_back.patient.application.dto.AddTreatmentRequest
import com.hazardev.fpc_back.patient.application.dto.ChangeStatusRequest
import com.hazardev.fpc_back.patient.application.dto.CompanionResponse
import com.hazardev.fpc_back.patient.application.dto.ContactResponse
import com.hazardev.fpc_back.patient.application.dto.CreatePatientRequest
import com.hazardev.fpc_back.patient.application.dto.DiagnosisRecordResponse
import com.hazardev.fpc_back.patient.application.dto.EnrollPatientRequest
import com.hazardev.fpc_back.patient.application.dto.InsuranceRecordResponse
import com.hazardev.fpc_back.patient.application.dto.LinkCompanionRequest
import com.hazardev.fpc_back.patient.application.dto.MedicalAppointmentResponse
import com.hazardev.fpc_back.patient.application.dto.PatientResponse
import com.hazardev.fpc_back.patient.application.dto.SisAffiliationResponse
import com.hazardev.fpc_back.patient.application.dto.TreatmentRecordResponse
import com.hazardev.fpc_back.patient.application.dto.UpdatePatientDetailsRequest
import com.hazardev.fpc_back.patient.application.dto.UpdatePatientRequest
import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * REST controller for Patient management.
 *
 * Exposes the full [PatientService] API via REST endpoints under `/api/patients`.
 * Handles patient CRUD, enrollment, insurance, diagnoses, treatments,
 * medical appointments, SIS affiliations, companions, and contact history.
 */
@RestController
@RequestMapping("/api/patients")
class PatientController(
    private val patientService: PatientService
) {

    // ═══════════════════════════════════════════════════════
    // Patient CRUD
    // ═══════════════════════════════════════════════════════

    @GetMapping
    fun getAllPatients(): List<PatientResponse> {
        return patientService.getAllPatients()
    }

    @GetMapping("/status/{status}")
    fun getPatientsByStatus(@PathVariable status: PatientStatus): List<PatientResponse> {
        return patientService.getPatientsByStatus(status)
    }

    @GetMapping("/role/{role}")
    fun getPatientsByRole(@PathVariable role: PatientRole): List<PatientResponse> {
        return patientService.getPatientsByRole(role)
    }

    @GetMapping("/{id}")
    fun getPatient(@PathVariable id: Long): PatientResponse {
        return patientService.getPatient(id)
    }

    @PostMapping
    fun createPatient(@RequestBody request: CreatePatientRequest): ResponseEntity<PatientResponse> {
        val response = patientService.createPatient(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun updatePatient(
        @PathVariable id: Long,
        @RequestBody request: UpdatePatientRequest
    ): PatientResponse {
        return patientService.updatePatient(id, request)
    }

    @PatchMapping("/{id}/status")
    fun changePatientStatus(
        @PathVariable id: Long,
        @RequestBody request: ChangeStatusRequest
    ): PatientResponse {
        return patientService.changePatientStatus(id, request.newStatus)
    }

    // ═══════════════════════════════════════════════════════
    // Patient Details (Enrollment)
    // ═══════════════════════════════════════════════════════

    @PostMapping("/{id}/enroll")
    fun enrollPatient(
        @PathVariable id: Long,
        @RequestBody request: EnrollPatientRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.enrollPatient(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}/details")
    fun updatePatientDetails(
        @PathVariable id: Long,
        @RequestBody request: UpdatePatientDetailsRequest
    ): PatientResponse {
        return patientService.updatePatientDetails(id, request)
    }

    // ═══════════════════════════════════════════════════════
    // Insurance
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/insurance")
    fun getInsuranceHistory(@PathVariable id: Long): List<InsuranceRecordResponse> {
        return patientService.getInsuranceHistory(id)
    }

    @PostMapping("/{id}/insurance")
    fun addInsurance(
        @PathVariable id: Long,
        @RequestBody request: AddInsuranceRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addInsurance(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ═══════════════════════════════════════════════════════
    // Diagnosis
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/diagnoses")
    fun getDiagnosisHistory(@PathVariable id: Long): List<DiagnosisRecordResponse> {
        return patientService.getDiagnosisHistory(id)
    }

    @PostMapping("/{id}/diagnoses")
    fun addDiagnosis(
        @PathVariable id: Long,
        @RequestBody request: AddDiagnosisRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addDiagnosis(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ═══════════════════════════════════════════════════════
    // Treatment
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/treatments")
    fun getTreatmentHistory(@PathVariable id: Long): List<TreatmentRecordResponse> {
        return patientService.getTreatmentHistory(id)
    }

    @PostMapping("/{id}/treatments")
    fun addTreatment(
        @PathVariable id: Long,
        @RequestBody request: AddTreatmentRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addTreatment(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ═══════════════════════════════════════════════════════
    // Medical Appointments
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/appointments")
    fun getMedicalAppointmentHistory(@PathVariable id: Long): List<MedicalAppointmentResponse> {
        return patientService.getMedicalAppointmentHistory(id)
    }

    @PostMapping("/{id}/appointments")
    fun addMedicalAppointment(
        @PathVariable id: Long,
        @RequestBody request: AddMedicalAppointmentRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addMedicalAppointment(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    // ═══════════════════════════════════════════════════════
    // SIS Affiliation
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/sis")
    fun getSisAffiliationHistory(@PathVariable id: Long): List<SisAffiliationResponse> {
        return patientService.getSisAffiliationHistory(id)
    }

    @PostMapping("/{id}/sis")
    fun addSisAffiliation(
        @PathVariable id: Long,
        @RequestBody request: AddSisAffiliationRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addSisAffiliation(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PatchMapping("/{id}/sis/{sisId}/affiliate")
    fun affiliateToSis(
        @PathVariable id: Long,
        @PathVariable sisId: Long
    ): PatientResponse {
        return patientService.affiliateToSis(id, sisId)
    }

    // ═══════════════════════════════════════════════════════
    // Companions (must appear before /{id}/companions routes)
    // ═══════════════════════════════════════════════════════

    @GetMapping("/companion/{companionId}/patients")
    fun getPatientsForCompanion(@PathVariable companionId: Long): List<PatientResponse> {
        return patientService.getPatientsForCompanion(companionId)
    }

    @GetMapping("/{id}/companions")
    fun getCompanions(@PathVariable id: Long): List<CompanionResponse> {
        return patientService.getCompanions(id)
    }

    @PostMapping("/{id}/companions")
    fun linkCompanion(
        @PathVariable id: Long,
        @RequestBody request: LinkCompanionRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.linkCompanion(
            patientId = id,
            companionId = request.companionId,
            isPrimaryInformant = request.isPrimaryInformant
        )
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{id}/companions/{companionId}")
    fun unlinkCompanion(
        @PathVariable id: Long,
        @PathVariable companionId: Long
    ): ResponseEntity<Void> {
        patientService.unlinkCompanion(id, companionId)
        return ResponseEntity.noContent().build()
    }

    // ═══════════════════════════════════════════════════════
    // Contact History
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/contacts")
    fun getContactHistory(@PathVariable id: Long): List<ContactResponse> {
        return patientService.getContactHistory(id)
    }
}
