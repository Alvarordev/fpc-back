package com.hazardev.fpc_back.alert.api

import com.hazardev.fpc_back.alert.application.AlertService
import com.hazardev.fpc_back.alert.application.dto.AlertResponse
import com.hazardev.fpc_back.alert.application.dto.CreateAlertRequest
import com.hazardev.fpc_back.alert.application.dto.ResolveAlertRequest
import com.hazardev.fpc_back.alert.application.dto.UpdateAlertRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/alerts")
class AlertController(
    private val alertService: AlertService
) {

    @GetMapping
    fun listAlerts(
        @RequestParam(required = false) healthCenterId: UUID?,
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) agentId: UUID?
    ): List<AlertResponse> {
        return when {
            healthCenterId != null -> alertService.getAlertsByHealthCenter(healthCenterId)
            status != null && status.uppercase() == "ACTIVE" -> alertService.getActiveAlerts()
            agentId != null -> alertService.getAlertsByAgent(agentId)
            else -> alertService.getAllAlerts()
        }
    }

    @GetMapping("/{id}")
    fun getAlertById(@PathVariable id: UUID): AlertResponse {
        return alertService.getAlertById(id)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createAlert(@RequestBody request: CreateAlertRequest): ResponseEntity<AlertResponse> {
        val response = alertService.createAlert(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateAlert(
        @PathVariable id: UUID,
        @RequestBody request: UpdateAlertRequest
    ): AlertResponse {
        return alertService.updateAlert(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deleteAlert(@PathVariable id: UUID): ResponseEntity<Void> {
        alertService.deleteAlert(id)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasRole('ADMIN')")
    fun resolveAlert(
        @PathVariable id: UUID,
        @RequestBody request: ResolveAlertRequest
    ): ResponseEntity<AlertResponse> {
        val response = alertService.resolveAlert(id, request)
        return ResponseEntity.ok(response)
    }
}
