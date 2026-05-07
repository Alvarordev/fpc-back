package com.hazardev.fpc_back.alert.infrastructure

import com.hazardev.fpc_back.alert.domain.Alert
import com.hazardev.fpc_back.shared.domain.AlertStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AlertRepository : JpaRepository<Alert, Long> {

    fun findByHealthCenterId(healthCenterId: Long): List<Alert>

    fun findByStatus(status: AlertStatus): List<Alert>

    fun findByCreatedById(agentId: UUID): List<Alert>
}
