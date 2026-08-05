package com.hazardev.fpc_back.alert.domain

import com.hazardev.fpc_back.agent.domain.Agent
import com.hazardev.fpc_back.contact.domain.Contact
import com.hazardev.fpc_back.healthcenter.domain.HealthCenter
import com.hazardev.fpc_back.shared.domain.AlertSeverity
import com.hazardev.fpc_back.shared.domain.AlertStatus
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
@Table(name = "alerts")
class Alert(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @Column(name = "ticket_number", nullable = true, unique = true)
    var ticketNumber: String? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "health_center_id", nullable = false)
    var healthCenter: HealthCenter,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contact_id", nullable = false)
    var contact: Contact,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by_id", nullable = false)
    var createdBy: Agent,

    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var description: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AlertStatus = AlertStatus.ACTIVE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var severity: AlertSeverity = AlertSeverity.HIGH,

    @Column(nullable = false)
    var category: String = "GENERAL",

    @Column(name = "under_review", nullable = false)
    var underReview: Boolean = false,

    @Column(name = "derived_to", nullable = true)
    var derivedTo: String? = null,

    @Column(name = "derivation_notes", nullable = true)
    var derivationNotes: String? = null,

    @Column(name = "ai_summary", columnDefinition = "TEXT", nullable = true)
    var aiSummary: String? = null,

    @Column(name = "resolved_at", nullable = true)
    var resolvedAt: LocalDateTime? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolved_by_id", nullable = true)
    var resolvedBy: Agent? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
