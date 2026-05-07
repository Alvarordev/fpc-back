package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.contact.infrastructure.ContactRepository
import com.hazardev.fpc_back.healthcenter.infrastructure.HealthCenterRepository
import com.hazardev.fpc_back.patient.application.dto.AddDiagnosisRequest
import com.hazardev.fpc_back.patient.application.dto.AddInsuranceRequest
import com.hazardev.fpc_back.patient.application.dto.AddMedicalAppointmentRequest
import com.hazardev.fpc_back.patient.application.dto.AddSisAffiliationRequest
import com.hazardev.fpc_back.patient.application.dto.AddTreatmentRequest
import com.hazardev.fpc_back.patient.application.dto.CompanionResponse
import com.hazardev.fpc_back.patient.application.dto.ContactResponse
import com.hazardev.fpc_back.patient.application.dto.ContactSummary
import com.hazardev.fpc_back.patient.application.dto.CreatePatientRequest
import com.hazardev.fpc_back.patient.application.dto.DiagnosisRecordResponse
import com.hazardev.fpc_back.patient.application.dto.DiagnosisSummary
import com.hazardev.fpc_back.patient.application.dto.EnrollPatientRequest
import com.hazardev.fpc_back.patient.application.dto.InsuranceRecordResponse
import com.hazardev.fpc_back.patient.application.dto.MedicalAppointmentResponse
import com.hazardev.fpc_back.patient.application.dto.PatientDetailsResponse
import com.hazardev.fpc_back.patient.application.dto.PatientResponse
import com.hazardev.fpc_back.patient.application.dto.SisAffiliationResponse
import com.hazardev.fpc_back.patient.application.dto.TreatmentRecordResponse
import com.hazardev.fpc_back.patient.application.dto.UpdatePatientDetailsRequest
import com.hazardev.fpc_back.patient.application.dto.UpdatePatientRequest
import com.hazardev.fpc_back.patient.domain.CompanionPatient
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.domain.PatientDetails
import com.hazardev.fpc_back.patient.domain.PatientDiagnosis
import com.hazardev.fpc_back.patient.domain.PatientInsurance
import com.hazardev.fpc_back.patient.domain.PatientMedicalAppointment
import com.hazardev.fpc_back.patient.domain.PatientSisAffiliation
import com.hazardev.fpc_back.patient.domain.PatientTreatment
import com.hazardev.fpc_back.patient.infrastructure.CompanionPatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientDetailsRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientDiagnosisRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientInsuranceRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientMedicalAppointmentRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSisAffiliationRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientTreatmentRepository
import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

/**
 * Centralized service for managing Patient entities and all their related sub-entities.
 *
 * This service is the single entry point for all patient CRUD operations,
 * including insurance history, diagnosis tracking, treatments, medical appointments,
 * SIS affiliation attempts, companion linking, and contact history.
 *
 * Business rules enforced:
 * - Never hard-delete: patients are deactivated by changing status to INACTIVE
 * - Strict status transitions: PROSPECT -> ENROLLED -> ACTIVE -> INACTIVE
 * - Current flag management: setting isCurrent=true on a new record sets all
 *   previous records for that patient to isCurrent=false
 * - DNI uniqueness validation on creation/update
 * - Referenced entity existence validation (Contact, HealthCenter, etc.)
 */
@Service
@Transactional
class PatientService(
    private val patientRepository: PatientRepository,
    private val patientDetailsRepository: PatientDetailsRepository,
    private val patientInsuranceRepository: PatientInsuranceRepository,
    private val patientDiagnosisRepository: PatientDiagnosisRepository,
    private val patientTreatmentRepository: PatientTreatmentRepository,
    private val patientMedicalAppointmentRepository: PatientMedicalAppointmentRepository,
    private val patientSisAffiliationRepository: PatientSisAffiliationRepository,
    private val companionPatientRepository: CompanionPatientRepository,
    private val contactRepository: ContactRepository,
    private val healthCenterRepository: HealthCenterRepository
) {

    // ═══════════════════════════════════════════════════════
    // Core Patient CRUD
    // ═══════════════════════════════════════════════════════

    /**
     * Create a new patient as a prospect.
     *
     * If a DNI is provided, validates uniqueness. The database also has
     * a unique constraint, but this provides a clear error message.
     *
     * @param request the patient creation data
     * @return the created patient with all related data (empty associations)
     * @throws IllegalArgumentException if the DNI is already registered
     */
    fun createPatient(request: CreatePatientRequest): PatientResponse {
        request.dni?.let { dni ->
            if (patientRepository.findByDni(dni) != null) {
                throw IllegalArgumentException(
                    "A patient with DNI '$dni' already exists. DNI must be unique."
                )
            }
        }

        val patient = Patient(
            fullName = request.fullName,
            dni = request.dni,
            birthDate = request.birthDate,
            primaryPhone = request.primaryPhone,
            secondaryPhone = request.secondaryPhone,
            hasWhatsapp = request.hasWhatsapp,
            role = request.role,
            status = request.status ?: PatientStatus.PROSPECT
        )

        return patientRepository.save(patient).let { saved ->
            buildPatientResponse(saved)
        }
    }

    /**
     * Get a patient by ID with ALL related data loaded.
     *
     * Returns the complete patient profile including details, insurance history,
     * diagnoses, treatments, medical appointments, SIS affiliations, companions,
     * and contact history.
     *
     * @param patientId the patient ID
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient does not exist
     */
    @Transactional(readOnly = true)
    fun getPatient(patientId: Long): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        return buildPatientResponse(patient)
    }

    /**
     * List all patients with complete related data.
     *
     * @return list of all patient responses
     */
    @Transactional(readOnly = true)
    fun getAllPatients(): List<PatientResponse> {
        return patientRepository.findAll().map { buildPatientResponse(it) }
    }

    /**
     * Filter patients by their current status.
     *
     * @param status the status to filter by
     * @return list of matching patient responses
     */
    @Transactional(readOnly = true)
    fun getPatientsByStatus(status: PatientStatus): List<PatientResponse> {
        return patientRepository.findByStatus(status).map { buildPatientResponse(it) }
    }

    /**
     * Filter patients by their role (PATIENT, COMPANION, UNKNOWN).
     *
     * @param role the role to filter by
     * @return list of matching patient responses
     */
    @Transactional(readOnly = true)
    fun getPatientsByRole(role: PatientRole): List<PatientResponse> {
        return patientRepository.findByRole(role).map { buildPatientResponse(it) }
    }

    /**
     * Update basic patient information.
     *
     * Only provided (non-null) fields are applied. If DNI is changed,
     * uniqueness is validated.
     *
     * @param patientId the patient ID
     * @param request the update data (all fields optional)
     * @return the updated patient response
     * @throws EntityNotFoundException if the patient does not exist
     * @throws IllegalArgumentException if the updated DNI is already in use
     */
    fun updatePatient(patientId: Long, request: UpdatePatientRequest): PatientResponse {
        val patient = findPatientOrThrow(patientId)

        request.dni?.let { newDni ->
            if (newDni != patient.dni) {
                val existing = patientRepository.findByDni(newDni)
                if (existing != null && existing.id != patientId) {
                    throw IllegalArgumentException(
                        "A patient with DNI '$newDni' already exists. DNI must be unique."
                    )
                }
                patient.dni = newDni
            }
        }

        request.fullName?.let { patient.fullName = it }
        request.birthDate?.let { patient.birthDate = it }
        request.primaryPhone?.let { patient.primaryPhone = it }
        request.secondaryPhone?.let { patient.secondaryPhone = it }
        request.hasWhatsapp?.let { patient.hasWhatsapp = it }
        request.role?.let { patient.role = it }

        return patientRepository.save(patient).let { buildPatientResponse(it) }
    }

    /**
     * Change the patient's status.
     *
     * Validates status transitions:
     * - PROSPECT can transition to ENROLLED or INACTIVE
     * - ENROLLED can transition to ACTIVE or INACTIVE
     * - ACTIVE can transition to INACTIVE
     * - INACTIVE is terminal (can be reverted manually)
     *
     * @param patientId the patient ID
     * @param newStatus the target status
     * @return the updated patient response
     * @throws EntityNotFoundException if the patient does not exist
     */
    fun changePatientStatus(patientId: Long, newStatus: PatientStatus): PatientResponse {
        val patient = findPatientOrThrow(patientId)

        val current = patient.status
        if (current == newStatus) {
            return buildPatientResponse(patient)
        }

        // Validate transition
        val allowed = when (current) {
            PatientStatus.PROSPECT ->
                newStatus in setOf(PatientStatus.ENROLLED, PatientStatus.INACTIVE)
            PatientStatus.ENROLLED ->
                newStatus in setOf(PatientStatus.ACTIVE, PatientStatus.INACTIVE)
            PatientStatus.ACTIVE ->
                newStatus == PatientStatus.INACTIVE
            PatientStatus.INACTIVE ->
                true // allow reactivation to any status
        }

        if (!allowed) {
            throw IllegalStateException(
                "Invalid status transition: cannot change from $current to $newStatus. " +
                    "Allowed transitions: PROSPECT->ENROLLED, PROSPECT->INACTIVE, " +
                    "ENROLLED->ACTIVE, ENROLLED->INACTIVE, ACTIVE->INACTIVE"
            )
        }

        patient.status = newStatus
        return patientRepository.save(patient).let { buildPatientResponse(it) }
    }

    /**
     * Soft-delete a patient by changing status to INACTIVE.
     *
     * Never performs a hard delete. This preserves all patient data and history.
     *
     * @param patientId the patient ID
     * @throws EntityNotFoundException if the patient does not exist
     */
    fun deletePatient(patientId: Long) {
        val patient = findPatientOrThrow(patientId)
        patient.status = PatientStatus.INACTIVE
        patientRepository.save(patient)
    }

    // ═══════════════════════════════════════════════════════
    // Patient Details
    // ═══════════════════════════════════════════════════════

    /**
     * Enroll a patient by creating their detailed record.
     *
     * Automatically changes the patient status from PROSPECT to ENROLLED.
     * A patient can only have one details record (enforced by unique constraint).
     *
     * @param patientId the patient ID
     * @param request the enrollment data
     * @return the complete patient response including the new details
     * @throws EntityNotFoundException if the patient does not exist
     * @throws IllegalStateException if the patient is not in PROSPECT status
     */
    fun enrollPatient(patientId: Long, request: EnrollPatientRequest): PatientResponse {
        val patient = findPatientOrThrow(patientId)

        if (patient.status != PatientStatus.PROSPECT) {
            throw IllegalStateException(
                "Cannot enroll patient $patientId: current status is ${patient.status}, " +
                    "expected PROSPECT"
            )
        }

        val details = PatientDetails(
            patient = patient,
            birthDepartment = request.birthDepartment,
            currentAddress = request.currentAddress,
            currentDistrict = request.currentDistrict,
            currentDepartment = request.currentDepartment,
            dniMatchesAddress = request.dniMatchesAddress,
            travelTimeToHospital = request.travelTimeToHospital,
            emergencyContactName = request.emergencyContactName,
            emergencyContactPhone = request.emergencyContactPhone,
            educationLevel = request.educationLevel,
            nativeLanguage = request.nativeLanguage,
            requiresTranslation = request.requiresTranslation
        )

        patientDetailsRepository.save(details)

        // Transition status to ENROLLED
        patient.status = PatientStatus.ENROLLED
        patientRepository.save(patient)

        return buildPatientResponse(patient)
    }

    /**
     * Update the patient's detailed record.
     *
     * Only provided (non-null) fields are applied.
     *
     * @param patientId the patient ID
     * @param request the update data (all fields optional)
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient does not exist or has no details record
     */
    fun updatePatientDetails(
        patientId: Long,
        request: UpdatePatientDetailsRequest
    ): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val details = patientDetailsRepository.findByPatientId(patientId)
            ?: throw EntityNotFoundException(
                "Patient details not found for patient $patientId. " +
                    "Enroll the patient first."
            )

        request.apply {
            birthDepartment?.let { details.birthDepartment = it }
            currentAddress?.let { details.currentAddress = it }
            currentDistrict?.let { details.currentDistrict = it }
            currentDepartment?.let { details.currentDepartment = it }
            dniMatchesAddress?.let { details.dniMatchesAddress = it }
            travelTimeToHospital?.let { details.travelTimeToHospital = it }
            emergencyContactName?.let { details.emergencyContactName = it }
            emergencyContactPhone?.let { details.emergencyContactPhone = it }
            educationLevel?.let { details.educationLevel = it }
            nativeLanguage?.let { details.nativeLanguage = it }
            requiresTranslation?.let { details.requiresTranslation = it }
        }

        patientDetailsRepository.save(details)
        return buildPatientResponse(patient)
    }

    // ═══════════════════════════════════════════════════════
    // Insurance
    // ═══════════════════════════════════════════════════════

    /**
     * Add a new insurance record for a patient (history-only, never updates).
     *
     * If the record is marked as current, all previous insurance records
     * for this patient are set to isCurrent=false.
     *
     * @param patientId the patient ID
     * @param request the insurance data
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient or contact does not exist
     */
    fun addInsurance(patientId: Long, request: AddInsuranceRequest): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)

        // If marked as current, set all previous to not current
        if (request.isCurrent) {
            patientInsuranceRepository.findByPatientIdAndIsCurrentTrue(patientId)
                .forEach { it.isCurrent = false }
        }

        val insurance = PatientInsurance(
            patient = patient,
            contact = contact,
            insuranceType = request.insuranceType,
            epsProvider = request.epsProvider,
            isCurrent = request.isCurrent,
            changeReason = request.changeReason,
            startDate = request.startDate,
            endDate = request.endDate
        )

        patientInsuranceRepository.save(insurance)
        return buildPatientResponse(patient)
    }

    /**
     * Get insurance history for a patient.
     *
     * @param patientId the patient ID
     * @return list of insurance records ordered by creation time (descending)
     */
    @Transactional(readOnly = true)
    fun getInsuranceHistory(patientId: Long): List<InsuranceRecordResponse> {
        return patientInsuranceRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    // ═══════════════════════════════════════════════════════
    // Diagnosis
    // ═══════════════════════════════════════════════════════

    /**
     * Add a new diagnosis record for a patient (history-only, never updates).
     *
     * If the record is marked as current, all previous diagnosis records
     * for this patient are set to isCurrent=false.
     *
     * @param patientId the patient ID
     * @param request the diagnosis data
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient, contact, or health center does not exist
     */
    fun addDiagnosis(patientId: Long, request: AddDiagnosisRequest): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)

        val healthCenter = request.healthCenterId?.let { id ->
            healthCenterRepository.findById(id)
                .orElseThrow { EntityNotFoundException("HealthCenter not found with id: $id") }
        }

        // If marked as current, set all previous to not current
        if (request.isCurrent) {
            patientDiagnosisRepository.findByPatientIdAndIsCurrentTrue(patientId)
                .forEach { it.isCurrent = false }
        }

        val diagnosis = PatientDiagnosis(
            patient = patient,
            contact = contact,
            diagnosis = request.diagnosis,
            cancerStage = request.cancerStage,
            diagnosisDate = request.diagnosisDate,
            healthCenter = healthCenter,
            diagnosisSpecialty = request.diagnosisSpecialty,
            symptomLeadingToCheckup = request.symptomLeadingToCheckup,
            waitTimeForDiagnosis = request.waitTimeForDiagnosis,
            hasMedicalReport = request.hasMedicalReport,
            isCurrent = request.isCurrent,
            changeReason = request.changeReason
        )

        patientDiagnosisRepository.save(diagnosis)
        return buildPatientResponse(patient)
    }

    /**
     * Get diagnosis history for a patient.
     *
     * @param patientId the patient ID
     * @return list of diagnosis records ordered by creation time (descending)
     */
    @Transactional(readOnly = true)
    fun getDiagnosisHistory(patientId: Long): List<DiagnosisRecordResponse> {
        return patientDiagnosisRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    // ═══════════════════════════════════════════════════════
    // Treatment
    // ═══════════════════════════════════════════════════════

    /**
     * Add a new treatment record linked to a diagnosis (history-only, never updates).
     *
     * If the record is marked as current, all previous treatment records
     * for this patient are set to isCurrent=false.
     *
     * @param patientId the patient ID
     * @param request the treatment data
     * @return the complete patient response
     * @throws EntityNotFoundException if any referenced entity does not exist
     */
    fun addTreatment(patientId: Long, request: AddTreatmentRequest): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)
        val diagnosis = patientDiagnosisRepository.findById(request.diagnosisId)
            .orElseThrow {
                EntityNotFoundException("PatientDiagnosis not found with id: ${request.diagnosisId}")
            }

        // Verify the diagnosis belongs to this patient
        if (diagnosis.patient.id != patientId) {
            throw IllegalArgumentException(
                "Diagnosis ${request.diagnosisId} does not belong to patient $patientId"
            )
        }

        val healthCenter = request.healthCenterId?.let { id ->
            healthCenterRepository.findById(id)
                .orElseThrow { EntityNotFoundException("HealthCenter not found with id: $id") }
        }

        // If marked as current, set all previous treatments for this patient to not current
        if (request.isCurrent) {
            patientTreatmentRepository.findByPatientIdAndIsCurrentTrue(patientId)
                .forEach { it.isCurrent = false }
        }

        val treatment = PatientTreatment(
            patient = patient,
            contact = contact,
            diagnosis = diagnosis,
            treatmentType = request.treatmentType,
            treatmentFrequency = request.treatmentFrequency,
            healthCenter = healthCenter,
            startDate = request.startDate,
            endDate = request.endDate,
            isCurrent = request.isCurrent,
            changeReason = request.changeReason,
            notReceivingReason = request.notReceivingReason
        )

        patientTreatmentRepository.save(treatment)
        return buildPatientResponse(patient)
    }

    /**
     * Get treatment history for a patient.
     *
     * @param patientId the patient ID
     * @return list of treatment records ordered by creation time (descending)
     */
    @Transactional(readOnly = true)
    fun getTreatmentHistory(patientId: Long): List<TreatmentRecordResponse> {
        return patientTreatmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    // ═══════════════════════════════════════════════════════
    // Medical Appointments
    // ═══════════════════════════════════════════════════════

    /**
     * Add a medical appointment record for a patient.
     *
     * @param patientId the patient ID
     * @param request the appointment data
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient, contact, or health center does not exist
     */
    fun addMedicalAppointment(
        patientId: Long,
        request: AddMedicalAppointmentRequest
    ): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)

        val healthCenter = request.healthCenterId?.let { id ->
            healthCenterRepository.findById(id)
                .orElseThrow { EntityNotFoundException("HealthCenter not found with id: $id") }
        }

        val appointment = PatientMedicalAppointment(
            patient = patient,
            contact = contact,
            healthCenter = healthCenter,
            specialty = request.specialty,
            appointmentDate = request.appointmentDate,
            nextAppointmentDate = request.nextAppointmentDate,
            hasReferralSheet = request.hasReferralSheet,
            referredTo = request.referredTo,
            difficulties = request.difficulties
        )

        patientMedicalAppointmentRepository.save(appointment)
        return buildPatientResponse(patient)
    }

    /**
     * Get medical appointment history for a patient.
     *
     * @param patientId the patient ID
     * @return list of appointment records ordered by creation time (descending)
     */
    @Transactional(readOnly = true)
    fun getMedicalAppointmentHistory(patientId: Long): List<MedicalAppointmentResponse> {
        return patientMedicalAppointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    // ═══════════════════════════════════════════════════════
    // SIS Affiliation
    // ═══════════════════════════════════════════════════════

    /**
     * Add a SIS affiliation attempt record for a patient.
     *
     * @param patientId the patient ID
     * @param request the affiliation data
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient or contact does not exist
     */
    fun addSisAffiliation(patientId: Long, request: AddSisAffiliationRequest): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)

        val affiliation = PatientSisAffiliation(
            patient = patient,
            contact = contact,
            canAffiliate = request.canAffiliate,
            expectedDate = request.expectedDate,
            cantAffiliateReason = request.cantAffiliateReason
        )

        patientSisAffiliationRepository.save(affiliation)
        return buildPatientResponse(patient)
    }

    /**
     * Mark a SIS affiliation record as affiliated (sets the timestamp).
     *
     * @param patientId the patient ID
     * @param sisRecordId the SIS affiliation record ID
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient or SIS record does not exist
     * @throws IllegalArgumentException if the SIS record does not belong to this patient
     */
    fun affiliateToSis(patientId: Long, sisRecordId: Long): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val sisRecord = patientSisAffiliationRepository.findById(sisRecordId)
            .orElseThrow {
                EntityNotFoundException("SIS affiliation record not found with id: $sisRecordId")
            }

        if (sisRecord.patient.id != patientId) {
            throw IllegalArgumentException(
                "SIS affiliation record $sisRecordId does not belong to patient $patientId"
            )
        }

        sisRecord.affiliatedAt = LocalDateTime.now()
        patientSisAffiliationRepository.save(sisRecord)

        return buildPatientResponse(patient)
    }

    /**
     * Get SIS affiliation history for a patient.
     *
     * @param patientId the patient ID
     * @return list of SIS affiliation records ordered by creation time (descending)
     */
    @Transactional(readOnly = true)
    fun getSisAffiliationHistory(patientId: Long): List<SisAffiliationResponse> {
        return patientSisAffiliationRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    // ═══════════════════════════════════════════════════════
    // Companion Management
    // ═══════════════════════════════════════════════════════

    /**
     * Link a companion to a patient.
     *
     * Validates that:
     * - The companion has role COMPANION
     * - The patient has role PATIENT
     * - The link does not already exist
     *
     * @param patientId the patient ID
     * @param companionId the companion's patient record ID (must have role COMPANION)
     * @param isPrimaryInformant whether this companion is the primary informant
     * @return the complete patient response
     * @throws EntityNotFoundException if either record does not exist
     * @throws IllegalArgumentException if role validation fails or link already exists
     */
    fun linkCompanion(
        patientId: Long,
        companionId: Long,
        isPrimaryInformant: Boolean = false
    ): PatientResponse {
        val patient = findPatientOrThrow(patientId)
        val companion = findPatientOrThrow(companionId)

        if (companion.role != PatientRole.COMPANION) {
            throw IllegalArgumentException(
                "Patient $companionId has role ${companion.role}, but COMPANION is required"
            )
        }

        if (patient.role != PatientRole.PATIENT) {
            throw IllegalArgumentException(
                "Patient $patientId has role ${patient.role}, but PATIENT is required"
            )
        }

        // Check for existing link
        val existing = companionPatientRepository.findByPatientIdAndCompanionId(
            patientId = patientId,
            companionId = companionId
        )
        if (existing != null) {
            throw IllegalArgumentException(
                "Companion $companionId is already linked to patient $patientId"
            )
        }

        val link = CompanionPatient(
            companion = companion,
            patient = patient,
            isPrimaryInformant = isPrimaryInformant
        )

        companionPatientRepository.save(link)
        return buildPatientResponse(patient)
    }

    /**
     * Remove the link between a patient and a companion.
     *
     * @param patientId the patient ID
     * @param companionId the companion's patient record ID
     * @throws EntityNotFoundException if the link does not exist
     */
    fun unlinkCompanion(patientId: Long, companionId: Long) {
        val link = companionPatientRepository.findByPatientIdAndCompanionId(
            patientId = patientId,
            companionId = companionId
        ) ?: throw EntityNotFoundException(
            "Link not found between patient $patientId and companion $companionId"
        )

        companionPatientRepository.delete(link)
    }

    /**
     * Get all companions linked to a patient.
     *
     * @param patientId the patient ID
     * @return list of companion responses
     */
    @Transactional(readOnly = true)
    fun getCompanions(patientId: Long): List<CompanionResponse> {
        return companionPatientRepository.findByPatientId(patientId)
            .map { it.toCompanionResponse() }
    }

    /**
     * Get all patients linked to a specific companion.
     *
     * @param companionId the companion's patient record ID
     * @return list of patient responses for all linked patients
     */
    @Transactional(readOnly = true)
    fun getPatientsForCompanion(companionId: Long): List<PatientResponse> {
        return companionPatientRepository.findByCompanionId(companionId)
            .map { buildPatientResponse(it.patient) }
    }

    // ═══════════════════════════════════════════════════════
    // Contact History
    // ═══════════════════════════════════════════════════════

    /**
     * Get contact history for a patient.
     *
     * @param patientId the patient ID
     * @return list of contact records ordered by creation time (descending)
     */
    @Transactional(readOnly = true)
    fun getContactHistory(patientId: Long): List<ContactResponse> {
        return contactRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    // ═══════════════════════════════════════════════════════
    // Private Helpers
    // ═══════════════════════════════════════════════════════

    /**
     * Find a patient by ID or throw a descriptive exception.
     */
    private fun findPatientOrThrow(patientId: Long): Patient {
        return patientRepository.findById(patientId)
            .orElseThrow { EntityNotFoundException("Patient not found with id: $patientId") }
    }

    /**
     * Find a contact by ID or throw a descriptive exception.
     */
    private fun findContactOrThrow(contactId: Long): Contact {
        return contactRepository.findById(contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with id: $contactId") }
    }

    /**
     * Build a complete PatientResponse by eagerly loading all related data
     * for the given patient.
     *
     * This method queries all sub-entity repositories within the current
     * transaction, ensuring lazy-loaded relationships can be resolved.
     */
    private fun buildPatientResponse(patient: Patient): PatientResponse {
        val patientId = patient.id!!

        val details = patientDetailsRepository.findByPatientId(patientId)
        val insuranceRecords = patientInsuranceRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val diagnosisRecords = patientDiagnosisRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val treatmentRecords = patientTreatmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val medicalAppointments = patientMedicalAppointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val sisAffiliations = patientSisAffiliationRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val companions = companionPatientRepository.findByPatientId(patientId)
        val contacts = contactRepository.findByPatientIdOrderByCreatedAtDesc(patientId)

        return PatientResponse(
            id = patientId,
            fullName = patient.fullName,
            dni = patient.dni,
            birthDate = patient.birthDate,
            primaryPhone = patient.primaryPhone,
            secondaryPhone = patient.secondaryPhone,
            hasWhatsapp = patient.hasWhatsapp,
            role = patient.role,
            status = patient.status,
            createdAt = patient.createdAt!!,
            updatedAt = patient.updatedAt!!,
            details = details?.toResponse(),
            insurance = insuranceRecords.map { it.toResponse() },
            diagnoses = diagnosisRecords.map { it.toResponse() },
            treatments = treatmentRecords.map { it.toResponse() },
            medicalAppointments = medicalAppointments.map { it.toResponse() },
            sisAffiliations = sisAffiliations.map { it.toResponse() },
            companions = companions.map { it.toCompanionResponse() },
            contacts = contacts.map { it.toResponse() }
        )
    }

    // ═══════════════════════════════════════════════════════
    // Entity-to-DTO Mappers
    // ═══════════════════════════════════════════════════════

    private fun PatientDetails.toResponse(): PatientDetailsResponse = PatientDetailsResponse(
        id = id!!,
        patientId = patient.id!!,
        birthDepartment = birthDepartment,
        currentAddress = currentAddress,
        currentDistrict = currentDistrict,
        currentDepartment = currentDepartment,
        dniMatchesAddress = dniMatchesAddress,
        travelTimeToHospital = travelTimeToHospital,
        emergencyContactName = emergencyContactName,
        emergencyContactPhone = emergencyContactPhone,
        educationLevel = educationLevel,
        nativeLanguage = nativeLanguage,
        requiresTranslation = requiresTranslation,
        createdAt = createdAt!!,
        updatedAt = updatedAt!!
    )

    private fun PatientInsurance.toResponse(): InsuranceRecordResponse = InsuranceRecordResponse(
        id = id!!,
        patientId = patient.id!!,
        insuranceType = insuranceType,
        epsProvider = epsProvider,
        isCurrent = isCurrent,
        changeReason = changeReason,
        startDate = startDate,
        endDate = endDate,
        createdAt = createdAt!!,
        contact = contact.toSummary()
    )

    private fun PatientDiagnosis.toResponse(): DiagnosisRecordResponse = DiagnosisRecordResponse(
        id = id!!,
        patientId = patient.id!!,
        diagnosis = diagnosis,
        cancerStage = cancerStage,
        diagnosisDate = diagnosisDate,
        healthCenterId = healthCenter?.id,
        healthCenterName = healthCenter?.name,
        diagnosisSpecialty = diagnosisSpecialty,
        symptomLeadingToCheckup = symptomLeadingToCheckup,
        waitTimeForDiagnosis = waitTimeForDiagnosis,
        hasMedicalReport = hasMedicalReport,
        isCurrent = isCurrent,
        changeReason = changeReason,
        createdAt = createdAt!!,
        contact = contact.toSummary()
    )

    private fun PatientTreatment.toResponse(): TreatmentRecordResponse = TreatmentRecordResponse(
        id = id!!,
        patientId = patient.id!!,
        diagnosis = DiagnosisSummary(
            id = diagnosis.id!!,
            diagnosis = diagnosis.diagnosis
        ),
        treatmentType = treatmentType,
        treatmentFrequency = treatmentFrequency,
        healthCenterId = healthCenter?.id,
        healthCenterName = healthCenter?.name,
        startDate = startDate,
        endDate = endDate,
        isCurrent = isCurrent,
        changeReason = changeReason,
        notReceivingReason = notReceivingReason,
        createdAt = createdAt!!,
        contact = contact.toSummary()
    )

    private fun PatientMedicalAppointment.toResponse(): MedicalAppointmentResponse =
        MedicalAppointmentResponse(
            id = id!!,
            patientId = patient.id!!,
            healthCenterId = healthCenter?.id,
            healthCenterName = healthCenter?.name,
            specialty = specialty,
            appointmentDate = appointmentDate,
            nextAppointmentDate = nextAppointmentDate,
            hasReferralSheet = hasReferralSheet,
            referredTo = referredTo,
            difficulties = difficulties,
            createdAt = createdAt!!,
            contact = contact.toSummary()
        )

    private fun PatientSisAffiliation.toResponse(): SisAffiliationResponse = SisAffiliationResponse(
        id = id!!,
        patientId = patient.id!!,
        contactId = contact.id!!,
        canAffiliate = canAffiliate,
        expectedDate = expectedDate,
        cantAffiliateReason = cantAffiliateReason,
        affiliatedAt = affiliatedAt,
        createdAt = createdAt!!
    )

    private fun CompanionPatient.toCompanionResponse(): CompanionResponse = CompanionResponse(
        companionId = companion.id!!,
        companionFullName = companion.fullName,
        isPrimaryInformant = isPrimaryInformant
    )

    private fun Contact.toResponse(): ContactResponse = ContactResponse(
        id = id!!,
        agentName = agent?.fullName,
        type = type,
        status = status,
        purpose = purpose,
        scheduledAt = scheduledAt,
        completedAt = completedAt,
        notes = notes,
        createdAt = createdAt!!
    )

    private fun Contact.toSummary(): ContactSummary = ContactSummary(
        id = id!!,
        agentName = agent?.fullName,
        type = type,
        status = status,
        purpose = purpose
    )
}
