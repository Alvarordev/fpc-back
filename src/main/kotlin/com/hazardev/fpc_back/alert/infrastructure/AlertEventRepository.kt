package com.hazardev.fpc_back.alert.infrastructure

import com.hazardev.fpc_back.alert.domain.AlertEvent
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface AlertEventRepository : JpaRepository<AlertEvent, UUID> {
    fun findByAlertIdOrderByCreatedAtAsc(alertId: UUID): List<AlertEvent>
}
