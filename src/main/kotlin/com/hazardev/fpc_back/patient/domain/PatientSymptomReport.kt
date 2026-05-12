package com.hazardev.fpc_back.patient.domain

import com.hazardev.fpc_back.contact.domain.Contact
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "patient_symptom_reports")
class PatientSymptomReport(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = true)
    var enrollment: Enrollment? = null,

    @Column(name = "discomfort_severity", nullable = true)
    var discomfortSeverity: String? = null,

    @Column(name = "discomfort_description", columnDefinition = "TEXT", nullable = true)
    var discomfortDescription: String? = null,

    @Column(name = "symptom_duration", nullable = true)
    var symptomDuration: String? = null,

    @Column(name = "symptom_frequency", nullable = true)
    var symptomFrequency: String? = null,

    @Column(name = "is_pain_present", nullable = true)
    var isPainPresent: Boolean? = null,

    @Column(name = "pain_intensity", nullable = true)
    var painIntensity: Int? = null,

    @Column(name = "pain_location", nullable = true)
    var painLocation: String? = null,

    @Column(name = "pain_description", columnDefinition = "TEXT", nullable = true)
    var painDescription: String? = null,

    @Column(name = "has_sought_medical_consultation", nullable = false)
    var hasSoughtMedicalConsultation: Boolean = false,

    @Column(name = "health_center_id")
    var healthCenterId: UUID? = null,

    @Column(name = "specialty")
    var specialty: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
