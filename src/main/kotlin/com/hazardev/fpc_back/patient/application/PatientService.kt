package com.hazardev.fpc_back.patient.application

import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
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
import com.hazardev.fpc_back.patient.application.dto.EnrollPatientDetailsRequest
import com.hazardev.fpc_back.patient.application.dto.EnrollPatientRequest
import com.hazardev.fpc_back.patient.application.dto.EnrollmentMetadataRequest
import com.hazardev.fpc_back.patient.application.dto.EnrollmentMetadataResponse
import com.hazardev.fpc_back.patient.application.dto.FullEnrollmentRequest
import com.hazardev.fpc_back.patient.application.dto.InsuranceRecordResponse
import com.hazardev.fpc_back.patient.application.dto.MedicalAppointmentResponse
import com.hazardev.fpc_back.patient.application.dto.PatientDetailsResponse
import com.hazardev.fpc_back.patient.application.dto.PatientResponse
import com.hazardev.fpc_back.patient.application.dto.SisAffiliationResponse
import com.hazardev.fpc_back.patient.application.dto.SymptomReportRequest
import com.hazardev.fpc_back.patient.application.dto.SymptomReportResponse
import com.hazardev.fpc_back.patient.application.dto.TreatmentRecordResponse
import com.hazardev.fpc_back.patient.application.dto.UpdatePatientDetailsRequest
import com.hazardev.fpc_back.patient.application.dto.UpdatePatientRequest
import com.hazardev.fpc_back.patient.domain.CompanionPatient
import com.hazardev.fpc_back.patient.domain.Enrollment
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.domain.PatientDetails
import com.hazardev.fpc_back.patient.domain.PatientDiagnosis
import com.hazardev.fpc_back.patient.domain.PatientInsurance
import com.hazardev.fpc_back.patient.domain.PatientMedicalAppointment
import com.hazardev.fpc_back.patient.domain.PatientSisAffiliation
import com.hazardev.fpc_back.patient.domain.PatientSymptomReport
import com.hazardev.fpc_back.patient.domain.PatientTreatment
import com.hazardev.fpc_back.patient.infrastructure.CompanionPatientRepository
import com.hazardev.fpc_back.patient.infrastructure.EnrollmentRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientDetailsRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientDiagnosisRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientInsuranceRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientMedicalAppointmentRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSisAffiliationRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSymptomReportRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientTreatmentRepository
import com.hazardev.fpc_back.shared.domain.AffiliationType
import com.hazardev.fpc_back.shared.domain.ContactPurpose
import com.hazardev.fpc_back.shared.domain.ContactStatus
import com.hazardev.fpc_back.shared.domain.ContactType
import com.hazardev.fpc_back.shared.domain.InsuranceType
import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.UUID

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
    private val healthCenterRepository: HealthCenterRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val patientSymptomReportRepository: PatientSymptomReportRepository,
    private val agentRepository: AgentRepository
) {

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
        val patient = createPatientEntity(request)
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
    fun getPatient(patientId: UUID): PatientResponse {
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
    fun updatePatient(patientId: UUID, request: UpdatePatientRequest): PatientResponse {
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
        request.gender?.let { patient.gender = it }
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
    fun changePatientStatus(patientId: UUID, newStatus: PatientStatus): PatientResponse {
        val patient = findPatientOrThrow(patientId)

        val current = patient.status
        if (current == newStatus) {
            return buildPatientResponse(patient)
        }

        val allowed = when (current) {
            PatientStatus.PROSPECT ->
                newStatus in setOf(PatientStatus.ENROLLED, PatientStatus.INACTIVE)
            PatientStatus.ENROLLED ->
                newStatus in setOf(PatientStatus.ACTIVE, PatientStatus.INACTIVE)
            PatientStatus.ACTIVE ->
                newStatus == PatientStatus.INACTIVE
            PatientStatus.INACTIVE ->
                true
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
    fun deletePatient(patientId: UUID) {
        val patient = findPatientOrThrow(patientId)
        patient.status = PatientStatus.INACTIVE
        patientRepository.save(patient)
    }

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
    fun enrollPatient(patientId: UUID, request: EnrollPatientRequest): PatientResponse {
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
            zoneType = request.zoneType,
            emergencyContactGender = request.emergencyContactGender,
            educationLevel = request.educationLevel,
            nativeLanguage = request.nativeLanguage,
            requiresTranslation = request.requiresTranslation
        )

        patientDetailsRepository.save(details)

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
        patientId: UUID,
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
            zoneType?.let { details.zoneType = it }
            emergencyContactGender?.let { details.emergencyContactGender = it }
            educationLevel?.let { details.educationLevel = it }
            nativeLanguage?.let { details.nativeLanguage = it }
            requiresTranslation?.let { details.requiresTranslation = it }
        }

        patientDetailsRepository.save(details)
        return buildPatientResponse(patient)
    }

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
    fun addInsurance(patientId: UUID, request: AddInsuranceRequest): PatientResponse {
        requireNotNull(request.contactId) { "contactId is required" }
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)

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
    fun getInsuranceHistory(patientId: UUID): List<InsuranceRecordResponse> {
        return patientInsuranceRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

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
    fun addDiagnosis(patientId: UUID, request: AddDiagnosisRequest): PatientResponse {
        requireNotNull(request.contactId) { "contactId is required" }
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)

        val healthCenter = request.healthCenterId?.let { id ->
            healthCenterRepository.findById(id)
                .orElseThrow { EntityNotFoundException("HealthCenter not found with id: $id") }
        }

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
    fun getDiagnosisHistory(patientId: UUID): List<DiagnosisRecordResponse> {
        return patientDiagnosisRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

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
    fun addTreatment(patientId: UUID, request: AddTreatmentRequest): PatientResponse {
        requireNotNull(request.contactId) { "contactId is required" }
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)
        val diagnosis = patientDiagnosisRepository.findById(request.diagnosisId)
            .orElseThrow {
                EntityNotFoundException("PatientDiagnosis not found with id: ${request.diagnosisId}")
            }
        if (diagnosis.patient.id != patientId) {
            throw IllegalArgumentException(
                "Diagnosis ${request.diagnosisId} does not belong to patient $patientId"
            )
        }

        val healthCenter = request.healthCenterId?.let { id ->
            healthCenterRepository.findById(id)
                .orElseThrow { EntityNotFoundException("HealthCenter not found with id: $id") }
        }

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
            notReceivingReason = request.notReceivingReason,
            treatmentSituation = request.treatmentSituation
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
    fun getTreatmentHistory(patientId: UUID): List<TreatmentRecordResponse> {
        return patientTreatmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    /**
     * Add a medical appointment record for a patient.
     *
     * @param patientId the patient ID
     * @param request the appointment data
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient, contact, or health center does not exist
     */
    fun addMedicalAppointment(
        patientId: UUID,
        request: AddMedicalAppointmentRequest
    ): PatientResponse {
        requireNotNull(request.contactId) { "contactId is required" }
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
            difficulties = request.difficulties,
            isFirstConsultation = request.isFirstConsultation
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
    fun getMedicalAppointmentHistory(patientId: UUID): List<MedicalAppointmentResponse> {
        return patientMedicalAppointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    /**
     * Add a SIS affiliation attempt record for a patient.
     *
     * @param patientId the patient ID
     * @param request the affiliation data
     * @return the complete patient response
     * @throws EntityNotFoundException if the patient or contact does not exist
     */
    fun addSisAffiliation(patientId: UUID, request: AddSisAffiliationRequest): PatientResponse {
        requireNotNull(request.contactId) { "contactId is required" }
        val patient = findPatientOrThrow(patientId)
        val contact = findContactOrThrow(request.contactId)

        val affiliation = PatientSisAffiliation(
            patient = patient,
            contact = contact,
            canAffiliate = request.canAffiliate,
            expectedDate = request.expectedDate,
            cantAffiliateReason = request.cantAffiliateReason,
            comments = request.comments
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
    fun affiliateToSis(patientId: UUID, sisRecordId: UUID): PatientResponse {
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
    fun getSisAffiliationHistory(patientId: UUID): List<SisAffiliationResponse> {
        return patientSisAffiliationRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

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
        patientId: UUID,
        companionId: UUID,
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
    fun unlinkCompanion(patientId: UUID, companionId: UUID) {
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
    fun getCompanions(patientId: UUID): List<CompanionResponse> {
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
    fun getPatientsForCompanion(companionId: UUID): List<PatientResponse> {
        return companionPatientRepository.findByCompanionId(companionId)
            .map { buildPatientResponse(it.patient) }
    }

    /**
     * Atomically create or enroll a patient with all related data in one transaction.
     *
     * Supports two modes:
     * - **Create mode** (patientId is null): creates a new patient from [request.patientData],
     *   then enrolls them with details, insurance, diagnosis, etc.
     * - **Update mode** (patientId is provided): fetches the existing patient, optionally
     *   updates basic info from [request.patientData], then processes all sub-entities.
     *
     * After successful processing, the patient's status is set to [PatientStatus.ENROLLED].
     *
     * Business rules enforced:
     * - [request.details] is required (enrollment needs PatientDetails)
     * - [request.patientData] is required when [request.patientId] is null
     * - DNI uniqueness validated on creation and on update (if DNI changes)
     * - SIS affiliation is only processed when there is no valid insurance
     *   (insurance is null or type is [InsuranceType.NONE])
     * - All sub-entity creation respects existing rules (isCurrent management,
     *   entity existence, companion role validation, etc.)
     *
     * @param request the composite enrollment payload
     * @return the complete patient response with all related data
     * @throws IllegalArgumentException if patientData is missing for new patients
     * @throws IllegalArgumentException if DNI is already in use
     * @throws EntityNotFoundException if a referenced entity does not exist
     */
    fun fullEnrollment(request: FullEnrollmentRequest): PatientResponse {
        require(request.details != null) {
            "details is required for enrollment"
        }

        val patient = if (request.patientId != null) {
            val p = findPatientOrThrow(request.patientId)

            if (request.patientData != null) {
                request.patientData.dni?.let { newDni ->
                    if (newDni != p.dni) {
                        val existing = patientRepository.findByDni(newDni)
                        if (existing != null && existing.id != p.id) {
                            throw IllegalArgumentException(
                                "A patient with DNI '$newDni' already exists. DNI must be unique."
                            )
                        }
                    }
                }

                p.fullName = request.patientData.fullName
                p.dni = request.patientData.dni
                p.birthDate = request.patientData.birthDate
                p.primaryPhone = request.patientData.primaryPhone
                p.secondaryPhone = request.patientData.secondaryPhone
                p.hasWhatsapp = request.patientData.hasWhatsapp
                p.gender = request.patientData.gender
                p.role = request.patientData.role
            }
            patientRepository.save(p)
        } else {
            require(request.patientData != null) {
                "patientData is required when patientId is null"
            }
            patientRepository.save(createPatientEntity(request.patientData))
        }

        val patientId = patient.id!!

        // Resolve or create enrollment contact — always available for sub-entities
        val enrollmentContact = resolveEnrollmentContact(patient, request.enrollmentMetadata)

        var enrollment: Enrollment? = null
        if (request.enrollmentMetadata != null) {
            enrollment = saveEnrollment(patient, enrollmentContact, request.enrollmentMetadata)
        }

        if (request.symptomReport != null) {
            saveSymptomReport(patient, enrollmentContact, enrollment, request.symptomReport)
        }

        createOrUpdateDetails(patient, request.details)

        if (request.insurance != null) {
            addInsurance(patientId, request.insurance.copy(contactId = enrollmentContact.id!!))
        }

        // Capture diagnosis ID so treatment can reference the newly-created diagnosis
        var diagnosisId: UUID? = null
        if (request.diagnosis != null) {
            val diagnosisResponse = addDiagnosis(
                patientId,
                request.diagnosis.copy(contactId = enrollmentContact.id!!)
            )
            diagnosisId = diagnosisResponse.diagnoses.firstOrNull()?.id
                ?: throw IllegalStateException("Failed to create diagnosis — no diagnosis found in response")
        }

        if (request.treatment != null) {
            // Use newly-created diagnosis ID if available, otherwise keep the one from the request
            val treatmentDiagnosisId = diagnosisId ?: request.treatment.diagnosisId
            addTreatment(
                patientId,
                request.treatment.copy(
                    contactId = enrollmentContact.id!!,
                    diagnosisId = treatmentDiagnosisId
                )
            )
        }

        request.medicalAppointments?.forEach { appointment ->
            addMedicalAppointment(
                patientId,
                appointment.copy(contactId = enrollmentContact.id!!)
            )
        }

        val hasNoRealInsurance = request.insurance == null ||
            request.insurance.insuranceType == InsuranceType.NONE
        if (hasNoRealInsurance && request.sisAffiliation != null) {
            addSisAffiliation(
                patientId,
                request.sisAffiliation.copy(contactId = enrollmentContact.id!!)
            )
        }

        request.companions?.forEach { companion ->
            linkCompanion(
                patientId = patientId,
                companionId = companion.companionId,
                isPrimaryInformant = companion.isPrimaryInformant
            )
        }

        patient.status = PatientStatus.ENROLLED
        patientRepository.save(patient)

        return getPatient(patientId)
    }

    /**
     * Get contact history for a patient.
     *
     * @param patientId the patient ID
     * @return list of contact records ordered by creation time (descending)
     */
    @Transactional(readOnly = true)
    fun getContactHistory(patientId: UUID): List<ContactResponse> {
        return contactRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
            .map { it.toResponse() }
    }

    private fun createPatientEntity(request: CreatePatientRequest): Patient {
        request.dni?.let { dni ->
            if (patientRepository.findByDni(dni) != null) {
                throw IllegalArgumentException(
                    "A patient with DNI '$dni' already exists. DNI must be unique."
                )
            }
        }

        return Patient(
            fullName = request.fullName,
            dni = request.dni,
            birthDate = request.birthDate,
            primaryPhone = request.primaryPhone,
            secondaryPhone = request.secondaryPhone,
            hasWhatsapp = request.hasWhatsapp,
            gender = request.gender,
            role = request.role,
            status = request.status ?: PatientStatus.PROSPECT
        )
    }

    private fun createOrUpdateDetails(patient: Patient, request: EnrollPatientDetailsRequest) {
        val existingDetails = patientDetailsRepository.findByPatientId(patient.id!!)

        if (existingDetails != null) {
            request.apply {
                birthDepartment?.let { existingDetails.birthDepartment = it }
                currentAddress?.let { existingDetails.currentAddress = it }
                currentDistrict?.let { existingDetails.currentDistrict = it }
                currentDepartment?.let { existingDetails.currentDepartment = it }
                dniMatchesAddress?.let { existingDetails.dniMatchesAddress = it }
                travelTimeToHospital?.let { existingDetails.travelTimeToHospital = it }
                emergencyContactName?.let { existingDetails.emergencyContactName = it }
                emergencyContactPhone?.let { existingDetails.emergencyContactPhone = it }
                zoneType?.let { existingDetails.zoneType = it }
                emergencyContactGender?.let { existingDetails.emergencyContactGender = it }
                educationLevel?.let { existingDetails.educationLevel = it }
                nativeLanguage?.let { existingDetails.nativeLanguage = it }
                existingDetails.requiresTranslation = requiresTranslation
            }
            patientDetailsRepository.save(existingDetails)
        } else {
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
                zoneType = request.zoneType,
                emergencyContactGender = request.emergencyContactGender,
                educationLevel = request.educationLevel,
                nativeLanguage = request.nativeLanguage,
                requiresTranslation = request.requiresTranslation
            )
            patientDetailsRepository.save(details)
        }
    }

    private fun findPatientOrThrow(patientId: UUID): Patient {
        return patientRepository.findById(patientId)
            .orElseThrow { EntityNotFoundException("Patient not found with id: $patientId") }
    }

    private fun findContactOrThrow(contactId: UUID): Contact {
        return contactRepository.findById(contactId)
            .orElseThrow { EntityNotFoundException("Contact not found with id: $contactId") }
    }

    /**
     * Resolve the enrollment contact for the patient.
     *
     * If a SCHEDULED contact with [ContactPurpose.ENROLLMENT] exists,
     * transition it to COMPLETED and update its fields. Otherwise, create
     * a new COMPLETED contact.
     *
     * When [enrollmentData] is null, still looks for an existing SCHEDULED
     * enrollment contact to upgrade, or creates a minimal contact so that
     * sub-entity operations always have a valid contact reference.
     *
     * @param patient the patient being enrolled
     * @param enrollmentData the enrollment metadata (may be null)
     * @return the resolved Contact — never null
     */
    private fun resolveEnrollmentContact(
        patient: Patient,
        enrollmentData: EnrollmentMetadataRequest?
    ): Contact {
        val agent = enrollmentData?.agentId?.let { agentId ->
            agentRepository.findById(agentId)
                .orElseThrow { EntityNotFoundException("Agent not found with id: $agentId") }
        }

        val scheduledAt = enrollmentData?.startTime?.let {
            LocalDateTime.ofInstant(it, ZoneOffset.UTC)
        }
        val completedAt = enrollmentData?.endTime?.let {
            LocalDateTime.ofInstant(it, ZoneOffset.UTC)
        } ?: LocalDateTime.now()

        val scheduledContact = contactRepository.findByPatientId(patient.id!!)
            .find { it.purpose == ContactPurpose.ENROLLMENT && it.status == ContactStatus.SCHEDULED }

        return if (scheduledContact != null) {
            scheduledContact.status = ContactStatus.COMPLETED
            scheduledContact.completedAt = completedAt
            if (agent != null) scheduledContact.agent = agent
            enrollmentData?.caseComments?.let { scheduledContact.notes = it }
            scheduledAt?.let { scheduledContact.scheduledAt = it }
            contactRepository.save(scheduledContact)
        } else {
            val newContact = Contact(
                patient = patient,
                agent = agent,
                type = ContactType.WHATSAPP,
                status = ContactStatus.COMPLETED,
                purpose = ContactPurpose.ENROLLMENT,
                scheduledAt = scheduledAt,
                completedAt = completedAt,
                notes = enrollmentData?.caseComments
            )
            contactRepository.save(newContact)
        }
    }

    /**
     * Create and save an [Enrollment] record linked to the patient and contact.
     */
    private fun saveEnrollment(
        patient: Patient,
        contact: Contact,
        enrollmentData: EnrollmentMetadataRequest
    ): Enrollment {
        val enrollment = Enrollment(
            patient = patient,
            contact = contact,
            currentlyAttendingConsultations = enrollmentData.currentlyAttendingConsultations,
            currentlyReceivingTreatment = enrollmentData.currentlyReceivingTreatment,
            entrySource = enrollmentData.programEntryPoint,
            consentToContact = enrollmentData.dataPolicyAccepted,
            consentToShareData = enrollmentData.informedConsentAccepted,
            affiliationType = enrollmentData.affiliationType,
            isOncologicalPatient = enrollmentData.isOncologicalPatient,
            surveyAccepted = enrollmentData.surveyAccepted
        )
        return enrollmentRepository.save(enrollment)
    }

    /**
     * Create and save a [PatientSymptomReport] linked to the patient, contact,
     * and optionally the enrollment.
     */
    private fun saveSymptomReport(
        patient: Patient,
        contact: Contact,
        enrollment: Enrollment?,
        symptomData: SymptomReportRequest
    ): PatientSymptomReport {
        val report = PatientSymptomReport(
            patient = patient,
            contact = contact,
            enrollment = enrollment,
            isPainPresent = symptomData.hasDiscomfort.takeIf { it },
            discomfortDescription = symptomData.signsAndSymptoms,
            painDescription = symptomData.firstConsultationDetails,
            discomfortSeverity = symptomData.indicationsReceived,
            hasSoughtMedicalConsultation = symptomData.hasSoughtMedicalConsultation,
            healthCenterId = symptomData.healthCenterId,
            specialty = symptomData.specialty
        )
        return patientSymptomReportRepository.save(report)
    }

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
        val enrollments = enrollmentRepository.findByPatientId(patientId)
        val symptomReports = patientSymptomReportRepository.findByPatientIdOrderByCreatedAtDesc(patientId)

        return PatientResponse(
            id = patientId,
            fullName = patient.fullName,
            dni = patient.dni,
            birthDate = patient.birthDate,
            primaryPhone = patient.primaryPhone,
            secondaryPhone = patient.secondaryPhone,
            hasWhatsapp = patient.hasWhatsapp,
            gender = patient.gender,
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
            contacts = contacts.map { it.toResponse() },
            enrollments = enrollments.map { it.toResponse() },
            symptomReports = symptomReports.map { it.toResponse() }
        )
    }


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
        zoneType = zoneType,
        emergencyContactGender = emergencyContactGender,
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
        treatmentSituation = treatmentSituation,
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
            isFirstConsultation = isFirstConsultation,
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
        comments = comments,
        affiliatedAt = affiliatedAt,
        createdAt = createdAt!!
    )

    private fun Enrollment.toResponse(): EnrollmentMetadataResponse = EnrollmentMetadataResponse(
        id = id!!,
        patientId = patient.id!!,
        contactId = contact.id!!,
        currentlyAttendingConsultations = currentlyAttendingConsultations,
        currentlyReceivingTreatment = currentlyReceivingTreatment,
        entrySource = entrySource,
        entrySubSource = entrySubSource,
        consentToContact = consentToContact,
        consentToShareData = consentToShareData,
        affiliationType = affiliationType,
        affiliatedPatientName = affiliatedPatientName,
        affiliatedPatientDni = affiliatedPatientDni,
        requiresTransportation = requiresTransportation,
        hasMobilityIssues = hasMobilityIssues,
        isOncologicalPatient = isOncologicalPatient,
        surveyAccepted = surveyAccepted,
        createdAt = createdAt!!
    )

    private fun PatientSymptomReport.toResponse(): SymptomReportResponse = SymptomReportResponse(
        id = id!!,
        patientId = patient.id!!,
        contactId = contact.id!!,
        enrollmentId = enrollment?.id,
        discomfortSeverity = discomfortSeverity,
        discomfortDescription = discomfortDescription,
        symptomDuration = symptomDuration,
        symptomFrequency = symptomFrequency,
        isPainPresent = isPainPresent,
        painIntensity = painIntensity,
        painLocation = painLocation,
        painDescription = painDescription,
        hasSoughtMedicalConsultation = hasSoughtMedicalConsultation,
        healthCenterId = healthCenterId,
        specialty = specialty,
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
