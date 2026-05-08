package com.hazardev.fpc_back.volunteer.domain

import com.hazardev.fpc_back.shared.domain.AvailabilityStatus
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
import jakarta.persistence.UniqueConstraint
import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.UUID

/**
 * Individual 1-hour availability slot for a volunteer.
 * The frontend splits date ranges into individual hour slots before sending.
 *
 * Unique constraint on (volunteer_id, date, start_time) prevents duplicate slots.
 */
@Entity
@Table(
    name = "volunteer_availability",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uq_volunteer_availability_slot",
            columnNames = ["volunteer_id", "date", "start_time"]
        )
    ]
)
class VolunteerAvailability(
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "volunteer_id", nullable = false)
    var volunteer: Volunteer,

    @Column(nullable = false)
    var date: LocalDate,

    @Column(name = "start_time", nullable = false)
    var startTime: LocalTime,

    @Column(name = "end_time", nullable = false)
    var endTime: LocalTime,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: AvailabilityStatus = AvailabilityStatus.AVAILABLE,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime? = null
)
