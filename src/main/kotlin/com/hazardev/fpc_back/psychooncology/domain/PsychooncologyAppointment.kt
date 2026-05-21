package com.hazardev.fpc_back.psychooncology.domain

import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.shared.domain.AppointmentModality
import com.hazardev.fpc_back.shared.domain.AppointmentStatus
import com.hazardev.fpc_back.shared.domain.ReferralType
import com.hazardev.fpc_back.volunteer.domain.Volunteer
import com.hazardev.fpc_back.volunteer.domain.VolunteerAvailability
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
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "psychooncology_appointments")
class PsychooncologyAppointment(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    var volunteer: Volunteer,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "availability_id", nullable = false)
    var availability: VolunteerAvailability,

    @Column(name = "patient_email", nullable = true)
    var patientEmail: String? = null,

    @Column(name = "session_number", nullable = false)
    var sessionNumber: Int,

    @Column(name = "is_additional_session", nullable = false)
    var isAdditionalSession: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var modality: AppointmentModality,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AppointmentStatus = AppointmentStatus.SCHEDULED,

    @Column(name = "scheduled_at", nullable = false)
    var scheduledAt: LocalDateTime,

    @Column(name = "completed_at", nullable = true)
    var completedAt: LocalDateTime? = null,

    @Column(name = "topic_addressed", nullable = true)
    var topicAddressed: String? = null,

    @Column(name = "session_details", nullable = true)
    var sessionDetails: String? = null,

    @Column(name = "additional_observations", nullable = true)
    var additionalObservations: String? = null,

    @Column(nullable = true)
    var recommendations: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    var referral: ReferralType? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
