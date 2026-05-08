package com.hazardev.fpc_back.healthcenter.infrastructure

import com.hazardev.fpc_back.healthcenter.domain.HealthCenter
import com.hazardev.fpc_back.shared.domain.PeruDepartment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface HealthCenterRepository : JpaRepository<HealthCenter, UUID> {

    fun findBySlug(slug: String): HealthCenter?

    fun existsBySlug(slug: String): Boolean

    fun findByIsActiveTrue(): List<HealthCenter>

    fun findByDepartment(department: PeruDepartment): List<HealthCenter>
}
