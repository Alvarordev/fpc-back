package com.hazardev.fpc_back.patient.domain

import com.hazardev.fpc_back.shared.domain.EducationLevel
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "patient_details")
class PatientDetails(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false, unique = true)
    var patient: Patient,

    @Column(name = "birth_department", nullable = true)
    var birthDepartment: String? = null,

    @Column(name = "current_address", nullable = true)
    var currentAddress: String? = null,

    @Column(name = "current_district", nullable = true)
    var currentDistrict: String? = null,

    @Column(name = "current_department", nullable = true)
    var currentDepartment: String? = null,

    @Column(name = "dni_matches_address", nullable = true)
    var dniMatchesAddress: Boolean? = null,

    @Column(name = "travel_time_to_hospital", nullable = true)
    var travelTimeToHospital: String? = null,

    @Column(name = "emergency_contact_name", nullable = true)
    var emergencyContactName: String? = null,

    @Column(name = "emergency_contact_phone", nullable = true)
    var emergencyContactPhone: String? = null,

    @Column(name = "zone_type", nullable = true)
    var zoneType: String? = null,

    @Column(name = "emergency_contact_gender", nullable = true)
    var emergencyContactGender: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "education_level", nullable = true)
    var educationLevel: EducationLevel? = null,

    @Column(name = "native_language", nullable = true)
    var nativeLanguage: String? = null,

    @Column(name = "requires_translation", nullable = false)
    var requiresTranslation: Boolean = false,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
