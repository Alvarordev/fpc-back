package com.hazardev.fpc_back.contact.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
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
@Table(name = "contact_service_referrals")
class ContactServiceReferral(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false, unique = true)
    var contact: Contact,

    @Column(name = "referred_to_social_worker")
    var referredToSocialWorker: Boolean? = null,

    @Column(name = "referred_to_susalud")
    var referredToSusalud: Boolean? = null,

    @Column(name = "susalud_registration_number")
    var susaludRegistrationNumber: String? = null,

    @Column(name = "received_food_guide")
    var receivedFoodGuide: Boolean? = null,

    @Column(name = "participates_in_gam")
    var participatesInGam: Boolean? = null,

    @Column(name = "program_satisfaction", columnDefinition = "TEXT")
    var programSatisfaction: String? = null,

    @Column(name = "wellbeing_changes", columnDefinition = "TEXT")
    var wellbeingChanges: String? = null,

    @Column(name = "knows_about_fissal")
    var knowsAboutFissal: Boolean? = null,

    @Column(name = "referred_to_paus")
    var referredToPaus: Boolean? = null,

    @Column(name = "referred_to_dae")
    var referredToDae: Boolean? = null,

    @Column(name = "referred_to_fissal")
    var referredToFissal: Boolean? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
