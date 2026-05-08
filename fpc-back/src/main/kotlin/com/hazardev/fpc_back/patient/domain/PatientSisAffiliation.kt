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
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "patient_sis_affiliation")
class PatientSisAffiliation(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @Column(name = "can_affiliate", nullable = false)
    var canAffiliate: Boolean,

    @Column(name = "expected_date", nullable = true)
    var expectedDate: LocalDate? = null,

    @Column(name = "cant_affiliate_reason", nullable = true)
    var cantAffiliateReason: String? = null,

    @Column(name = "affiliated_at", nullable = true)
    var affiliatedAt: LocalDateTime? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
