package com.hazardev.fpc_back.patient.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.MapsId
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "patient_summaries")
class PatientSummary(
    @Id
    @Column(name = "patient_id", nullable = false)
    val patientId: UUID? = null,

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "patient_id", nullable = false)
    var patient: Patient,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PatientSummaryStatus = PatientSummaryStatus.PENDING,

    @Column(name = "summary_json")
    var summaryJson: String? = null,

    @Column(name = "generated_at")
    var generatedAt: LocalDateTime? = null,

    @Column(name = "generated_from_source_updated_at")
    var generatedFromSourceUpdatedAt: LocalDateTime? = null,

    @Column(name = "retry_count", nullable = false)
    var retryCount: Int = 0,

    @Column(name = "last_attempt_at")
    var lastAttemptAt: LocalDateTime? = null,

    @Column(name = "next_attempt_at")
    var nextAttemptAt: LocalDateTime? = null,

    @Column(name = "processing_started_at")
    var processingStartedAt: LocalDateTime? = null,

    @Column(name = "last_error_code")
    var lastErrorCode: String? = null,

    @Column(name = "last_error_message")
    var lastErrorMessage: String? = null,

    @Column(name = "schema_version", nullable = false)
    var schemaVersion: Int = 1,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: LocalDateTime? = null
)
