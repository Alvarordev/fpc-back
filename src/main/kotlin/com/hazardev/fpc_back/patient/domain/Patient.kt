package com.hazardev.fpc_back.patient.domain

import com.hazardev.fpc_back.shared.domain.PatientRole
import com.hazardev.fpc_back.shared.domain.PatientStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "patients")
class Patient(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "full_name", nullable = false)
    var fullName: String,

    @Column(nullable = true, unique = true)
    var dni: String? = null,

    @Column(name = "birth_date", nullable = true)
    var birthDate: LocalDate? = null,

    @Column(name = "primary_phone", nullable = false)
    var primaryPhone: String,

    @Column(name = "gender", nullable = true)
    var gender: String? = null,

    @Column(name = "secondary_phone", nullable = true)
    var secondaryPhone: String? = null,

    @Column(name = "has_whatsapp", nullable = false)
    var hasWhatsapp: Boolean = false,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var role: PatientRole = PatientRole.UNKNOWN,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PatientStatus = PatientStatus.PROSPECT,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
