package com.hazardev.fpc_back.patient.application.dto

import com.hazardev.fpc_back.shared.domain.CancerStage
import com.hazardev.fpc_back.shared.domain.ContactPurpose
import com.hazardev.fpc_back.shared.domain.ContactStatus
import com.hazardev.fpc_back.shared.domain.ContactType
import com.hazardev.fpc_back.shared.domain.EducationLevel
import com.hazardev.fpc_back.shared.domain.EpsProvider
import com.hazardev.fpc_back.shared.domain.InsuranceType
import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import java.time.LocalDate
import java.time.LocalDateTime

// ═══════════════════════════════════════════════════════════
// Shared summary types used across multiple response DTOs
// ═══════════════════════════════════════════════════════════

data class ContactSummary(
    val id: Long,
    val agentName: String?,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose
)

data class DiagnosisSummary(
    val id: Long,
    val diagnosis: String
)

// ═══════════════════════════════════════════════════════════
// Core Patient DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Request to create a new patient (prospect).
 */
data class CreatePatientRequest(
    val fullName: String,
    val dni: String? = null,
    val birthDate: LocalDate? = null,
    val primaryPhone: String,
    val secondaryPhone: String? = null,
    val hasWhatsapp: Boolean = false,
    val role: PatientRole = PatientRole.UNKNOWN,
    val status: PatientStatus? = null
)

/**
 * Request to update basic patient information.
 * All fields are optional — only provided fields will be updated.
 */
data class UpdatePatientRequest(
    val fullName: String? = null,
    val dni: String? = null,
    val birthDate: LocalDate? = null,
    val primaryPhone: String? = null,
    val secondaryPhone: String? = null,
    val hasWhatsapp: Boolean? = null,
    val role: PatientRole? = null
)

/**
 * Complete patient response including all related entities.
 */
data class PatientResponse(
    val id: Long,
    val fullName: String,
    val dni: String?,
    val birthDate: LocalDate?,
    val primaryPhone: String,
    val secondaryPhone: String?,
    val hasWhatsapp: Boolean,
    val role: PatientRole,
    val status: PatientStatus,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime,
    val details: PatientDetailsResponse?,
    val insurance: List<InsuranceRecordResponse>,
    val diagnoses: List<DiagnosisRecordResponse>,
    val treatments: List<TreatmentRecordResponse>,
    val medicalAppointments: List<MedicalAppointmentResponse>,
    val sisAffiliations: List<SisAffiliationResponse>,
    val companions: List<CompanionResponse>,
    val contacts: List<ContactResponse>
)

// ═══════════════════════════════════════════════════════════
// Patient Details DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Request to enroll a patient by creating their detailed record.
 * Triggers status change from PROSPECT to ENROLLED.
 */
data class EnrollPatientRequest(
    val birthDepartment: String? = null,
    val currentAddress: String? = null,
    val currentDistrict: String? = null,
    val currentDepartment: String? = null,
    val dniMatchesAddress: Boolean? = null,
    val travelTimeToHospital: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val educationLevel: EducationLevel? = null,
    val nativeLanguage: String? = null,
    val requiresTranslation: Boolean = false
)

/**
 * Request with PatientDetails fields for full enrollment flow.
 * Mirrors [EnrollPatientRequest] but used as a sub-object of [FullEnrollmentRequest].
 */
data class EnrollPatientDetailsRequest(
    val birthDepartment: String? = null,
    val currentAddress: String? = null,
    val currentDistrict: String? = null,
    val currentDepartment: String? = null,
    val dniMatchesAddress: Boolean? = null,
    val travelTimeToHospital: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val educationLevel: EducationLevel? = null,
    val nativeLanguage: String? = null,
    val requiresTranslation: Boolean = false
)

/**
 * Request to update existing patient details.
 * All fields are optional.
 */
data class UpdatePatientDetailsRequest(
    val birthDepartment: String? = null,
    val currentAddress: String? = null,
    val currentDistrict: String? = null,
    val currentDepartment: String? = null,
    val dniMatchesAddress: Boolean? = null,
    val travelTimeToHospital: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val educationLevel: EducationLevel? = null,
    val nativeLanguage: String? = null,
    val requiresTranslation: Boolean? = null
)

/**
 * Response representation of patient details.
 */
data class PatientDetailsResponse(
    val id: Long,
    val patientId: Long,
    val birthDepartment: String?,
    val currentAddress: String?,
    val currentDistrict: String?,
    val currentDepartment: String?,
    val dniMatchesAddress: Boolean?,
    val travelTimeToHospital: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val educationLevel: EducationLevel?,
    val nativeLanguage: String?,
    val requiresTranslation: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

// ═══════════════════════════════════════════════════════════
// Insurance DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Request to add a new insurance record for a patient.
 */
data class AddInsuranceRequest(
    val insuranceType: InsuranceType,
    val epsProvider: EpsProvider? = null,
    val isCurrent: Boolean,
    val changeReason: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val contactId: Long
)

/**
 * Response for an insurance history record.
 */
data class InsuranceRecordResponse(
    val id: Long,
    val patientId: Long,
    val insuranceType: InsuranceType,
    val epsProvider: EpsProvider?,
    val isCurrent: Boolean,
    val changeReason: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val createdAt: LocalDateTime,
    val contact: ContactSummary
)

// ═══════════════════════════════════════════════════════════
// Diagnosis DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Request to add a new diagnosis record for a patient.
 */
data class AddDiagnosisRequest(
    val diagnosis: String,
    val cancerStage: CancerStage? = null,
    val diagnosisDate: LocalDate? = null,
    val healthCenterId: Long? = null,
    val diagnosisSpecialty: String? = null,
    val symptomLeadingToCheckup: String? = null,
    val waitTimeForDiagnosis: String? = null,
    val hasMedicalReport: Boolean = false,
    val isCurrent: Boolean,
    val changeReason: String? = null,
    val contactId: Long
)

/**
 * Response for a diagnosis history record.
 */
data class DiagnosisRecordResponse(
    val id: Long,
    val patientId: Long,
    val diagnosis: String,
    val cancerStage: CancerStage?,
    val diagnosisDate: LocalDate?,
    val healthCenterId: Long?,
    val healthCenterName: String?,
    val diagnosisSpecialty: String?,
    val symptomLeadingToCheckup: String?,
    val waitTimeForDiagnosis: String?,
    val hasMedicalReport: Boolean,
    val isCurrent: Boolean,
    val changeReason: String?,
    val createdAt: LocalDateTime,
    val contact: ContactSummary
)

// ═══════════════════════════════════════════════════════════
// Treatment DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Request to add a new treatment record linked to a diagnosis.
 */
data class AddTreatmentRequest(
    val diagnosisId: Long,
    val treatmentType: String,
    val treatmentFrequency: String? = null,
    val healthCenterId: Long? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isCurrent: Boolean,
    val changeReason: String? = null,
    val notReceivingReason: String? = null,
    val contactId: Long
)

/**
 * Response for a treatment history record.
 */
data class TreatmentRecordResponse(
    val id: Long,
    val patientId: Long,
    val diagnosis: DiagnosisSummary,
    val treatmentType: String,
    val treatmentFrequency: String?,
    val healthCenterId: Long?,
    val healthCenterName: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val isCurrent: Boolean,
    val changeReason: String?,
    val notReceivingReason: String?,
    val createdAt: LocalDateTime,
    val contact: ContactSummary
)

// ═══════════════════════════════════════════════════════════
// Medical Appointment DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Request to add a medical appointment record for a patient.
 */
data class AddMedicalAppointmentRequest(
    val healthCenterId: Long? = null,
    val specialty: String? = null,
    val appointmentDate: LocalDate? = null,
    val nextAppointmentDate: LocalDate? = null,
    val hasReferralSheet: Boolean = false,
    val referredTo: String? = null,
    val difficulties: String? = null,
    val contactId: Long
)

/**
 * Response for a medical appointment record.
 */
data class MedicalAppointmentResponse(
    val id: Long,
    val patientId: Long,
    val healthCenterId: Long?,
    val healthCenterName: String?,
    val specialty: String?,
    val appointmentDate: LocalDate?,
    val nextAppointmentDate: LocalDate?,
    val hasReferralSheet: Boolean,
    val referredTo: String?,
    val difficulties: String?,
    val createdAt: LocalDateTime,
    val contact: ContactSummary
)

// ═══════════════════════════════════════════════════════════
// SIS Affiliation DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Request to add a SIS affiliation attempt for a patient.
 */
data class AddSisAffiliationRequest(
    val canAffiliate: Boolean,
    val expectedDate: LocalDate? = null,
    val cantAffiliateReason: String? = null,
    val contactId: Long
)

/**
 * Response for a SIS affiliation record.
 */
data class SisAffiliationResponse(
    val id: Long,
    val patientId: Long,
    val contactId: Long,
    val canAffiliate: Boolean,
    val expectedDate: LocalDate?,
    val cantAffiliateReason: String?,
    val affiliatedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

// ═══════════════════════════════════════════════════════════
// Companion DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Response for a companion linked to a patient.
 */
data class CompanionResponse(
    val companionId: Long,
    val companionFullName: String,
    val isPrimaryInformant: Boolean
)

// ═══════════════════════════════════════════════════════════
// Contact DTOs
// ═══════════════════════════════════════════════════════════

/**
 * Response for a contact record in the patient's contact history.
 */
data class ContactResponse(
    val id: Long,
    val agentName: String?,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose,
    val scheduledAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val notes: String?,
    val createdAt: LocalDateTime
)

// ═══════════════════════════════════════════════════════════
// Full Enrollment DTO
// ═══════════════════════════════════════════════════════════

/**
 * Composite request for atomically creating/enrolling a patient
 * in a single transaction. All sub-entities (details, insurance,
 * diagnosis, treatment, appointments, SIS, companions) are
 * processed together.
 *
 * @property patientId If provided, enrolls an existing patient;
 *                     if null, a new patient is created from [patientData]
 * @property patientData Required when [patientId] is null
 * @property details PatientDetails — required for enrollment
 * @property insurance Optional insurance record
 * @property diagnosis Optional diagnosis record
 * @property treatment Optional treatment record (requires diagnosis)
 * @property medicalAppointments Optional list of appointments
 * @property sisAffiliation Optional SIS affiliation (processed only when
 *                          no real insurance exists)
 * @property companions Optional list of companions to link
 * @property contactId The contact that initiated this enrollment
 */
data class FullEnrollmentRequest(
    val patientId: Long?,
    val patientData: CreatePatientRequest?,
    val details: EnrollPatientDetailsRequest?,
    val insurance: AddInsuranceRequest?,
    val diagnosis: AddDiagnosisRequest?,
    val treatment: AddTreatmentRequest?,
    val medicalAppointments: List<AddMedicalAppointmentRequest>?,
    val sisAffiliation: AddSisAffiliationRequest?,
    val companions: List<LinkCompanionRequest>?,
    val contactId: Long?
)
