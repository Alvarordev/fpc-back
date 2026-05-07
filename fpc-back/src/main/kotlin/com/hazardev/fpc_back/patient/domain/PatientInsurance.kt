package com.hazardev.fpc_back.patient.domain

import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.shared.domain.EpsProvider
import com.hazardev.fpc_back.shared.domain.InsuranceType
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
@Table(name = "patient_insurance")
class PatientInsurance(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @Enumerated(EnumType.STRING)
    @Column(name = "insurance_type", nullable = false)
    var insuranceType: InsuranceType,

    @Enumerated(EnumType.STRING)
    @Column(name = "eps_provider", nullable = true)
    var epsProvider: EpsProvider? = null,

    @Column(name = "is_current", nullable = false)
    var isCurrent: Boolean,

    @Column(name = "change_reason", nullable = true)
    var changeReason: String? = null,

    @Column(name = "start_date", nullable = true)
    var startDate: LocalDate? = null,

    @Column(name = "end_date", nullable = true)
    var endDate: LocalDate? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
