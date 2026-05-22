package com.hazardev.fpc_back.patient.api

import com.hazardev.fpc_back.patient.application.PatientService
import com.hazardev.fpc_back.patient.application.PatientSummaryService
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
import com.hazardev.fpc_back.patient.application.dto.FullEnrollmentRequest
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
import jakarta.validation.Valid
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
import java.util.UUID

@RestController
@RequestMapping("/api/patients")
class PatientController(
    private val patientService: PatientService,
    private val patientSummaryService: PatientSummaryService
) {

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
    fun getPatient(@PathVariable id: UUID): PatientResponse {
        return patientService.getPatient(id)
    }

    @PostMapping
    fun createPatient(@RequestBody request: CreatePatientRequest): ResponseEntity<PatientResponse> {
        val response = patientService.createPatient(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    fun updatePatient(
        @PathVariable id: UUID,
        @RequestBody request: UpdatePatientRequest
    ): PatientResponse {
        return patientService.updatePatient(id, request)
    }

    @PatchMapping("/{id}/status")
    fun changePatientStatus(
        @PathVariable id: UUID,
        @RequestBody request: ChangeStatusRequest
    ): PatientResponse {
        return patientService.changePatientStatus(id, request.newStatus)
    }

    @PostMapping("/enroll")
    fun fullEnrollment(
        @Valid @RequestBody request: FullEnrollmentRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.fullEnrollment(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PostMapping("/{id}/enroll")
    fun enrollPatient(
        @PathVariable id: UUID,
        @RequestBody request: EnrollPatientRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.enrollPatient(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}/details")
    fun updatePatientDetails(
        @PathVariable id: UUID,
        @RequestBody request: UpdatePatientDetailsRequest
    ): PatientResponse {
        return patientService.updatePatientDetails(id, request)
    }

    @GetMapping("/{id}/insurance")
    fun getInsuranceHistory(@PathVariable id: UUID): List<InsuranceRecordResponse> {
        return patientService.getInsuranceHistory(id)
    }

    @PostMapping("/{id}/insurance")
    fun addInsurance(
        @PathVariable id: UUID,
        @RequestBody request: AddInsuranceRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addInsurance(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}/diagnoses")
    fun getDiagnosisHistory(@PathVariable id: UUID): List<DiagnosisRecordResponse> {
        return patientService.getDiagnosisHistory(id)
    }

    @PostMapping("/{id}/diagnoses")
    fun addDiagnosis(
        @PathVariable id: UUID,
        @RequestBody request: AddDiagnosisRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addDiagnosis(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}/treatments")
    fun getTreatmentHistory(@PathVariable id: UUID): List<TreatmentRecordResponse> {
        return patientService.getTreatmentHistory(id)
    }

    @PostMapping("/{id}/treatments")
    fun addTreatment(
        @PathVariable id: UUID,
        @RequestBody request: AddTreatmentRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addTreatment(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}/appointments")
    fun getMedicalAppointmentHistory(@PathVariable id: UUID): List<MedicalAppointmentResponse> {
        return patientService.getMedicalAppointmentHistory(id)
    }

    @PostMapping("/{id}/appointments")
    fun addMedicalAppointment(
        @PathVariable id: UUID,
        @RequestBody request: AddMedicalAppointmentRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addMedicalAppointment(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/{id}/sis")
    fun getSisAffiliationHistory(@PathVariable id: UUID): List<SisAffiliationResponse> {
        return patientService.getSisAffiliationHistory(id)
    }

    @PostMapping("/{id}/sis")
    fun addSisAffiliation(
        @PathVariable id: UUID,
        @RequestBody request: AddSisAffiliationRequest
    ): ResponseEntity<PatientResponse> {
        val response = patientService.addSisAffiliation(id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PatchMapping("/{id}/sis/{sisId}/affiliate")
    fun affiliateToSis(
        @PathVariable id: UUID,
        @PathVariable sisId: UUID
    ): PatientResponse {
        return patientService.affiliateToSis(id, sisId)
    }

    @GetMapping("/companion/{companionId}/patients")
    fun getPatientsForCompanion(@PathVariable companionId: UUID): List<PatientResponse> {
        return patientService.getPatientsForCompanion(companionId)
    }

    @GetMapping("/{id}/companions")
    fun getCompanions(@PathVariable id: UUID): List<CompanionResponse> {
        return patientService.getCompanions(id)
    }

    @PostMapping("/{id}/companions")
    fun linkCompanion(
        @PathVariable id: UUID,
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
        @PathVariable id: UUID,
        @PathVariable companionId: UUID
    ): ResponseEntity<Void> {
        patientService.unlinkCompanion(id, companionId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{id}/contacts")
    fun getContactHistory(@PathVariable id: UUID): List<ContactResponse> {
        return patientService.getContactHistory(id)
    }

    @GetMapping("/dni/{dni}/summary")
    fun getPatientSummaryByDni(@PathVariable dni: String): ResponseEntity<String> {
        val summary = patientSummaryService.generateSummaryByDni(dni)
        return ResponseEntity.ok()
            .header("Content-Type", "application/json")
            .body(summary)
    }
}
