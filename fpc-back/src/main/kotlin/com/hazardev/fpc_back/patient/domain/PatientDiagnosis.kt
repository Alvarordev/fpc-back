package com.hazardev.fpc_back.patient.domain

import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.healthcenter.domain.HealthCenter
import com.hazardev.fpc_back.shared.domain.CancerStage
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(name = "patient_diagnoses")
class PatientDiagnosis(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @Column(nullable = false)
    var diagnosis: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "cancer_stage", nullable = true)
    var cancerStage: CancerStage? = null,

    @Column(name = "diagnosis_date", nullable = true)
    var diagnosisDate: LocalDate? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_center_id", nullable = true)
    var healthCenter: HealthCenter? = null,

    @Column(name = "diagnosis_specialty", nullable = true)
    var diagnosisSpecialty: String? = null,

    @Column(name = "symptom_leading_to_checkup", nullable = true)
    var symptomLeadingToCheckup: String? = null,

    @Column(name = "wait_time_for_diagnosis", nullable = true)
    var waitTimeForDiagnosis: String? = null,

    @Column(name = "has_medical_report", nullable = false)
    var hasMedicalReport: Boolean = false,

    @Column(name = "is_current", nullable = false)
    var isCurrent: Boolean,

    @Column(name = "change_reason", nullable = true)
    var changeReason: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
