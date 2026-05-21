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
import java.util.UUID

@Repository
interface VolunteerAvailabilityRepository : JpaRepository<VolunteerAvailability, UUID> {

    fun findByVolunteerIdAndDateBetween(
        volunteerId: UUID,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<VolunteerAvailability>

    fun findByStatusAndVolunteerId(
        status: AvailabilityStatus,
        volunteerId: UUID
    ): List<VolunteerAvailability>

    fun findByVolunteerId(volunteerId: UUID): List<VolunteerAvailability>

    fun existsByVolunteerIdAndDateAndStartTime(
        volunteerId: UUID,
        date: LocalDate,
        startTime: java.time.LocalTime
    ): Boolean

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT va FROM VolunteerAvailability va WHERE va.id = :id")
    fun findByIdWithLock(@Param("id") id: UUID): Optional<VolunteerAvailability>
}
