package com.hazardev.fpc_back.contact.domain

import com.hazardev.fpc_back.agent.domain.Agent
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.shared.domain.ContactPurpose
import com.hazardev.fpc_back.shared.domain.ContactStatus
import com.hazardev.fpc_back.shared.domain.ContactType
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

@Entity
@Table(name = "contacts")
class Contact(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = true)
    var agent: Agent? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var type: ContactType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: ContactStatus,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var purpose: ContactPurpose,

    @Column(name = "scheduled_at", nullable = true)
    var scheduledAt: LocalDateTime? = null,

    @Column(name = "completed_at", nullable = true)
    var completedAt: LocalDateTime? = null,

    @Column(nullable = true)
    var notes: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_next_contact_id", nullable = true)
    var scheduledNextContact: Contact? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
