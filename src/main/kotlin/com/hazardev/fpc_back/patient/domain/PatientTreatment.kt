package com.hazardev.fpc_back.patient.domain

import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.healthcenter.domain.HealthCenter
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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "patient_treatments")
class PatientTreatment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "diagnosis_id", nullable = false)
    var diagnosis: PatientDiagnosis,

    @Column(name = "treatment_type", nullable = false)
    var treatmentType: String,

    @Column(name = "treatment_frequency", nullable = true)
    var treatmentFrequency: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_center_id", nullable = true)
    var healthCenter: HealthCenter? = null,

    @Column(name = "start_date", nullable = true)
    var startDate: LocalDate? = null,

    @Column(name = "end_date", nullable = true)
    var endDate: LocalDate? = null,

    @Column(name = "is_current", nullable = false)
    var isCurrent: Boolean,

    @Column(name = "change_reason", nullable = true)
    var changeReason: String? = null,

    @Column(name = "not_receiving_reason", nullable = true)
    var notReceivingReason: String? = null,

    @Column(name = "treatment_situation", nullable = true)
    var treatmentSituation: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
