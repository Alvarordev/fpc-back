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

@Entity
@Table(name = "patient_medical_appointments")
class PatientMedicalAppointment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "health_center_id", nullable = true)
    var healthCenter: HealthCenter? = null,

    @Column(nullable = true)
    var specialty: String? = null,

    @Column(name = "appointment_date", nullable = true)
    var appointmentDate: LocalDate? = null,

    @Column(name = "next_appointment_date", nullable = true)
    var nextAppointmentDate: LocalDate? = null,

    @Column(name = "has_referral_sheet", nullable = false)
    var hasReferralSheet: Boolean = false,

    @Column(name = "referred_to", nullable = true)
    var referredTo: String? = null,

    @Column(nullable = true)
    var difficulties: String? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
