package com.hazardev.fpc_back.patient.application.dto

import com.hazardev.fpc_back.shared.domain.AffiliationType
import com.hazardev.fpc_back.shared.domain.CancerStage
import com.hazardev.fpc_back.shared.domain.ContactPurpose
import com.hazardev.fpc_back.shared.domain.ContactStatus
import com.hazardev.fpc_back.shared.domain.ContactType
import com.hazardev.fpc_back.shared.domain.EducationLevel
import com.hazardev.fpc_back.shared.domain.EpsProvider
import com.hazardev.fpc_back.shared.domain.InsuranceType
import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class ContactSummary(
    val id: UUID,
    val agentName: String?,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose
)

data class DiagnosisSummary(
    val id: UUID,
    val diagnosis: String
)

data class CreatePatientRequest(
    val fullName: String,
    val dni: String? = null,
    val birthDate: LocalDate? = null,
    val primaryPhone: String,
    val secondaryPhone: String? = null,
    val hasWhatsapp: Boolean = false,
    val gender: String? = null,
    val role: PatientRole = PatientRole.UNKNOWN,
    val status: PatientStatus? = null
)

data class UpdatePatientRequest(
    val fullName: String? = null,
    val dni: String? = null,
    val birthDate: LocalDate? = null,
    val primaryPhone: String? = null,
    val secondaryPhone: String? = null,
    val hasWhatsapp: Boolean? = null,
    val gender: String? = null,
    val role: PatientRole? = null
)

data class PatientResponse(
    val id: UUID,
    val fullName: String,
    val dni: String?,
    val birthDate: LocalDate?,
    val primaryPhone: String,
    val secondaryPhone: String?,
    val hasWhatsapp: Boolean,
    val gender: String?,
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
    val contacts: List<ContactResponse>,
    val enrollments: List<EnrollmentMetadataResponse>,
    val symptomReports: List<SymptomReportResponse>
)

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
    val zoneType: String? = null,
    val emergencyContactGender: String? = null,
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
        val zoneType: String? = null,
        val emergencyContactGender: String? = null,
        val educationLevel: EducationLevel? = null,
        val nativeLanguage: String? = null,
        val requiresTranslation: Boolean = false
    )

data class UpdatePatientDetailsRequest(
    val birthDepartment: String? = null,
    val currentAddress: String? = null,
    val currentDistrict: String? = null,
    val currentDepartment: String? = null,
    val dniMatchesAddress: Boolean? = null,
    val travelTimeToHospital: String? = null,
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val zoneType: String? = null,
    val emergencyContactGender: String? = null,
    val educationLevel: EducationLevel? = null,
    val nativeLanguage: String? = null,
    val requiresTranslation: Boolean? = null
)

data class PatientDetailsResponse(
    val id: UUID,
    val patientId: UUID,
    val birthDepartment: String?,
    val currentAddress: String?,
    val currentDistrict: String?,
    val currentDepartment: String?,
    val dniMatchesAddress: Boolean?,
    val travelTimeToHospital: String?,
    val emergencyContactName: String?,
    val emergencyContactPhone: String?,
    val zoneType: String?,
    val emergencyContactGender: String?,
    val educationLevel: EducationLevel?,
    val nativeLanguage: String?,
    val requiresTranslation: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class AddInsuranceRequest(
    val insuranceType: InsuranceType,
    val epsProvider: EpsProvider? = null,
    val isCurrent: Boolean,
    val changeReason: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val contactId: UUID? = null
)

data class InsuranceRecordResponse(
    val id: UUID,
    val patientId: UUID,
    val insuranceType: InsuranceType,
    val epsProvider: EpsProvider?,
    val isCurrent: Boolean,
    val changeReason: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val createdAt: LocalDateTime,
    val contact: ContactSummary
)

data class AddDiagnosisRequest(
    val diagnosis: String,
    val cancerStage: CancerStage? = null,
    val diagnosisDate: LocalDate? = null,
    val healthCenterId: UUID? = null,
    val diagnosisSpecialty: String? = null,
    val symptomLeadingToCheckup: String? = null,
    val waitTimeForDiagnosis: String? = null,
    val hasMedicalReport: Boolean = false,
    val isCurrent: Boolean,
    val changeReason: String? = null,
    val contactId: UUID? = null
)

data class DiagnosisRecordResponse(
    val id: UUID,
    val patientId: UUID,
    val diagnosis: String,
    val cancerStage: CancerStage?,
    val diagnosisDate: LocalDate?,
    val healthCenterId: UUID?,
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

data class AddTreatmentRequest(
    val diagnosisId: UUID,
    val treatmentType: String,
    val treatmentFrequency: String? = null,
    val healthCenterId: UUID? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isCurrent: Boolean,
    val changeReason: String? = null,
    val notReceivingReason: String? = null,
    val treatmentSituation: String? = null,
    val contactId: UUID? = null
)

data class TreatmentRecordResponse(
    val id: UUID,
    val patientId: UUID,
    val diagnosis: DiagnosisSummary,
    val treatmentType: String,
    val treatmentFrequency: String?,
    val healthCenterId: UUID?,
    val healthCenterName: String?,
    val startDate: LocalDate?,
    val endDate: LocalDate?,
    val isCurrent: Boolean,
    val changeReason: String?,
    val notReceivingReason: String?,
    val treatmentSituation: String?,
    val createdAt: LocalDateTime,
    val contact: ContactSummary
)

data class AddMedicalAppointmentRequest(
    val healthCenterId: UUID? = null,
    val specialty: String? = null,
    val appointmentDate: LocalDate? = null,
    val nextAppointmentDate: LocalDate? = null,
    val hasReferralSheet: Boolean = false,
    val referredTo: String? = null,
    val difficulties: String? = null,
    val isFirstConsultation: Boolean = false,
    val contactId: UUID? = null
)

data class MedicalAppointmentResponse(
    val id: UUID,
    val patientId: UUID,
    val healthCenterId: UUID?,
    val healthCenterName: String?,
    val specialty: String?,
    val appointmentDate: LocalDate?,
    val nextAppointmentDate: LocalDate?,
    val hasReferralSheet: Boolean,
    val referredTo: String?,
    val difficulties: String?,
    val isFirstConsultation: Boolean,
    val createdAt: LocalDateTime,
    val contact: ContactSummary
)

data class AddSisAffiliationRequest(
    val canAffiliate: Boolean,
    val expectedDate: LocalDate? = null,
    val cantAffiliateReason: String? = null,
    val comments: String? = null,
    val contactId: UUID? = null
)

data class SisAffiliationResponse(
    val id: UUID,
    val patientId: UUID,
    val contactId: UUID,
    val canAffiliate: Boolean,
    val expectedDate: LocalDate?,
    val cantAffiliateReason: String?,
    val comments: String?,
    val affiliatedAt: LocalDateTime?,
    val createdAt: LocalDateTime
)

data class CompanionResponse(
    val companionId: UUID,
    val companionFullName: String,
    val isPrimaryInformant: Boolean
)

data class ContactResponse(
    val id: UUID,
    val agentName: String?,
    val type: ContactType,
    val status: ContactStatus,
    val purpose: ContactPurpose,
    val scheduledAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val notes: String?,
    val createdAt: LocalDateTime
)

/**
 * Composite request for atomically creating/enrolling a patient
 * in a single transaction. All sub-entities (details, insurance,
 * diagnosis, treatment, appointments, SIS, companions) are
 * processed together. The enrollment contact is resolved/created
 * automatically — no contactId is required anywhere in the request.
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
 */
data class FullEnrollmentRequest(
    val patientId: UUID?,
    val patientData: CreatePatientRequest?,
    val details: EnrollPatientDetailsRequest?,
    val insurance: AddInsuranceRequest?,
    val diagnosis: AddDiagnosisRequest?,
    val treatment: AddTreatmentRequest?,
    val medicalAppointments: List<AddMedicalAppointmentRequest>?,
    val sisAffiliation: AddSisAffiliationRequest?,
    val companions: List<LinkCompanionRequest>?,
    val enrollmentMetadata: EnrollmentMetadataRequest? = null,
    val symptomReport: SymptomReportRequest? = null
)

/**
 * Request DTO for enrollment wizard metadata.
 *
 * Fields like [caseComments], [startTime], [endTime], and [agentId] are used
 * for the enrollment Contact lifecycle. The remaining fields map to the
 * [com.hazardev.fpc_back.patient.domain.Enrollment] entity.
 */
data class EnrollmentMetadataRequest(
    val caseComments: String? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
    val dataPolicyAccepted: Boolean = false,
    val informedConsentAccepted: Boolean = false,
    val affiliationType: AffiliationType = AffiliationType.PATIENT,
    val isOncologicalPatient: Boolean = false,
    val programEntryPoint: String? = null,
    val currentlyAttendingConsultations: Boolean? = null,
    val currentlyReceivingTreatment: Boolean? = null,
    val surveyAccepted: Boolean = false,
    val agentId: UUID? = null
)

/**
 * Response DTO for an enrollment record.
 * Mirrors the [com.hazardev.fpc_back.patient.domain.Enrollment] entity fields
 * plus relational identifiers.
 */
data class EnrollmentMetadataResponse(
    val id: UUID,
    val patientId: UUID,
    val contactId: UUID,
    val currentlyAttendingConsultations: Boolean?,
    val currentlyReceivingTreatment: Boolean?,
    val entrySource: String?,
    val entrySubSource: String?,
    val consentToContact: Boolean?,
    val consentToShareData: Boolean?,
    val affiliationType: AffiliationType?,
    val affiliatedPatientName: String?,
    val affiliatedPatientDni: String?,
    val requiresTransportation: Boolean?,
    val hasMobilityIssues: Boolean?,
    val isOncologicalPatient: Boolean,
    val surveyAccepted: Boolean,
    val createdAt: LocalDateTime
)

/**
 * Request DTO for wizard symptom report data.
 */
data class SymptomReportRequest(
    val hasDiscomfort: Boolean = false,
    val signsAndSymptoms: String? = null,
    val hasSoughtMedicalConsultation: Boolean = false,
    val healthCenterId: UUID? = null,
    val specialty: String? = null,
    val firstConsultationDetails: String? = null,
    val indicationsReceived: String? = null
)

/**
 * Response DTO for a patient symptom report.
 * Mirrors the [com.hazardev.fpc_back.patient.domain.PatientSymptomReport] entity fields
 * plus relational identifiers.
 */
data class SymptomReportResponse(
    val id: UUID,
    val patientId: UUID,
    val contactId: UUID,
    val enrollmentId: UUID?,
    val discomfortSeverity: String?,
    val discomfortDescription: String?,
    val symptomDuration: String?,
    val symptomFrequency: String?,
    val isPainPresent: Boolean?,
    val painIntensity: Int?,
    val painLocation: String?,
    val painDescription: String?,
    val hasSoughtMedicalConsultation: Boolean,
    val healthCenterId: UUID?,
    val specialty: String?,
    val createdAt: LocalDateTime
)
