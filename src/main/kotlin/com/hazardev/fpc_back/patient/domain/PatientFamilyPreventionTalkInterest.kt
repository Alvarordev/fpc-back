package com.hazardev.fpc_back.patient.domain

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
@Table(name = "patient_family_prevention_talk_interests")
class PatientFamilyPreventionTalkInterest(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @Column(name = "talk_name", nullable = false)
    var talkName: String,

    @Column(name = "family_member_name", nullable = false)
    var familyMemberName: String,

    @Column(name = "family_member_phone", nullable = false)
    var familyMemberPhone: String,

    @Column(name = "family_member_email", nullable = false)
    var familyMemberEmail: String,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
