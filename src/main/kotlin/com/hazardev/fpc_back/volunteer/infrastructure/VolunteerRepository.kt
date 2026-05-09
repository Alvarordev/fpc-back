package com.hazardev.fpc_back.volunteer.infrastructure

import com.hazardev.fpc_back.volunteer.domain.Volunteer
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface VolunteerRepository : JpaRepository<Volunteer, UUID> {
    fun existsByUserId(userId: java.util.UUID): Boolean
    fun findByUserId(userId: java.util.UUID): Volunteer?
}
