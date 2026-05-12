package com.hazardev.fpc_back.patient.domain

import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.shared.domain.AffiliationType
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
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "enrollments")
class Enrollment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @Column(name = "currently_attending_consultations", nullable = true)
    var currentlyAttendingConsultations: Boolean? = null,

    @Column(name = "currently_receiving_treatment", nullable = true)
    var currentlyReceivingTreatment: Boolean? = null,

    @Column(name = "entry_source", nullable = true)
    var entrySource: String? = null,

    @Column(name = "entry_sub_source", nullable = true)
    var entrySubSource: String? = null,

    @Column(name = "consent_to_contact", nullable = true)
    var consentToContact: Boolean? = null,

    @Column(name = "consent_to_share_data", nullable = true)
    var consentToShareData: Boolean? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "affiliation_type", nullable = true)
    var affiliationType: AffiliationType? = null,

    @Column(name = "affiliated_patient_name", nullable = true)
    var affiliatedPatientName: String? = null,

    @Column(name = "affiliated_patient_dni", nullable = true)
    var affiliatedPatientDni: String? = null,

    @Column(name = "requires_transportation", nullable = true)
    var requiresTransportation: Boolean? = null,

    @Column(name = "has_mobility_issues", nullable = true)
    var hasMobilityIssues: Boolean? = null,

    @Column(name = "is_oncological_patient", nullable = false)
    var isOncologicalPatient: Boolean = false,

    @Column(name = "survey_accepted", nullable = false)
    var surveyAccepted: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
