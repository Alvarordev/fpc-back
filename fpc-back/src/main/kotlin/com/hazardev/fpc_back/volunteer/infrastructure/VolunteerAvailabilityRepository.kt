package com.hazardev.fpc_back.volunteer.infrastructure

import com.hazardev.fpc_back.shared.domain.AvailabilityStatus
import com.hazardev.fpc_back.volunteer.domain.VolunteerAvailability
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.Optional

@Repository
interface VolunteerAvailabilityRepository : JpaRepository<VolunteerAvailability, Long> {

    /**
     * Find all availability slots for a volunteer within a date range.
     */
    fun findByVolunteerIdAndDateBetween(
        volunteerId: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<VolunteerAvailability>

    /**
     * Find availability slots by status and volunteer.
     */
    fun findByStatusAndVolunteerId(
        status: AvailabilityStatus,
        volunteerId: Long
    ): List<VolunteerAvailability>

    /**
     * Find all availability slots for a specific volunteer.
     */
    fun findByVolunteerId(volunteerId: Long): List<VolunteerAvailability>

    /**
     * Check if a duplicate slot exists for the same volunteer, date, and start time.
     */
    fun existsByVolunteerIdAndDateAndStartTime(
        volunteerId: Long,
        date: LocalDate,
        startTime: java.time.LocalTime
    ): Boolean

    /**
     * Find an availability slot by ID with a pessimistic write lock to prevent race conditions
     * when reserving a slot. This acquires a SELECT ... FOR UPDATE lock on the row.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT va FROM VolunteerAvailability va WHERE va.id = :id")
    fun findByIdWithLock(@Param("id") id: Long): Optional<VolunteerAvailability>
}
