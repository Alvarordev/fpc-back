package com.hazardev.fpc_back.healthcenter.api

import com.hazardev.fpc_back.healthcenter.application.HealthCenterService
import com.hazardev.fpc_back.healthcenter.application.dto.CreateHealthCenterRequest
import com.hazardev.fpc_back.healthcenter.application.dto.HealthCenterResponse
import com.hazardev.fpc_back.healthcenter.application.dto.UpdateHealthCenterRequest
import com.hazardev.fpc_back.shared.domain.PeruDepartment
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/health-centers")
class HealthCenterController(
    private val healthCenterService: HealthCenterService
) {

    @GetMapping
    fun listAll(
        @RequestParam(required = false) department: PeruDepartment?
    ): List<HealthCenterResponse> {
        return if (department != null) {
            healthCenterService.getByDepartment(department)
        } else {
            healthCenterService.getAllHealthCenters()
        }
    }

    @GetMapping("/{id}")
    fun getById(@PathVariable id: UUID): HealthCenterResponse {
        return healthCenterService.getById(id)
    }

    @GetMapping("/slug/{slug}")
    fun getBySlug(@PathVariable slug: String): HealthCenterResponse {
        return healthCenterService.getBySlug(slug)
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createHealthCenter(@RequestBody request: CreateHealthCenterRequest): ResponseEntity<HealthCenterResponse> {
        val response = healthCenterService.createHealthCenter(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun updateHealthCenter(
        @PathVariable id: UUID,
        @RequestBody request: UpdateHealthCenterRequest
    ): HealthCenterResponse {
        return healthCenterService.updateHealthCenter(id, request)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    fun deactivateHealthCenter(@PathVariable id: UUID): ResponseEntity<Void> {
        healthCenterService.deactivateHealthCenter(id)
        return ResponseEntity.noContent().build()
    }

    @PatchMapping("/{id}/reactivate")
    @PreAuthorize("hasRole('ADMIN')")
    fun reactivateHealthCenter(@PathVariable id: UUID): HealthCenterResponse {
        return healthCenterService.reactivateHealthCenter(id)
    }
}
