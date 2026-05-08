package com.hazardev.fpc_back.healthcenter.application

import com.hazardev.fpc_back.healthcenter.application.dto.CreateHealthCenterRequest
import com.hazardev.fpc_back.healthcenter.application.dto.HealthCenterResponse
import com.hazardev.fpc_back.healthcenter.application.dto.UpdateHealthCenterRequest
import com.hazardev.fpc_back.healthcenter.domain.HealthCenter
import com.hazardev.fpc_back.healthcenter.infrastructure.HealthCenterRepository
import com.hazardev.fpc_back.shared.domain.PeruDepartment
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer
import java.util.UUID

/**
 * Manages health center entities with slug generation and lifecycle operations.
 */
@Service
class HealthCenterService(
    private val healthCenterRepository: HealthCenterRepository
) {

    companion object {
        private val logger = LoggerFactory.getLogger(HealthCenterService::class.java)
    }

    /**
     * Create a new health center with an auto-generated slug from the name.
     *
     * Slug generation rules:
     * 1. Normalize accented characters to their base ASCII equivalents (e.g., á → a, ñ → n)
     * 2. Convert to lowercase
     * 3. Replace whitespace with hyphens
     * 4. Remove any remaining non-alphanumeric characters except hyphens
     * 5. Collapse consecutive hyphens into a single hyphen
     * 6. Trim leading and trailing hyphens
     *
     * Business rules:
     * - If the generated slug already exists, throws [IllegalStateException]
     * - Creates the health center with [isActive] set to true by default
     *
     * @param request containing name and department
     * @return the created health center as a response DTO
     * @throws IllegalStateException if the generated slug is already in use
     */
    @Transactional
    fun createHealthCenter(request: CreateHealthCenterRequest): HealthCenterResponse {
        val slug = generateSlug(request.name)

        if (healthCenterRepository.existsBySlug(slug)) {
            throw IllegalStateException(
                "A health center with slug '$slug' already exists (from name: '${request.name}')"
            )
        }

        val healthCenter = HealthCenter(
            name = request.name,
            slug = slug,
            department = request.department
        )

        val saved = healthCenterRepository.save(healthCenter)
        logger.info("Created health center: id={}, name={}, slug={}", saved.id, saved.name, saved.slug)
        return saved.toResponse()
    }

    fun getAllHealthCenters(): List<HealthCenterResponse> {
        return healthCenterRepository.findByIsActiveTrue()
            .map { it.toResponse() }
    }

    fun getBySlug(slug: String): HealthCenterResponse {
        val healthCenter = healthCenterRepository.findBySlug(slug)
            ?: throw EntityNotFoundException("Health center not found with slug: $slug")
        return healthCenter.toResponse()
    }

    fun getByDepartment(department: PeruDepartment): List<HealthCenterResponse> {
        return healthCenterRepository.findByDepartment(department)
            .map { it.toResponse() }
    }

    /**
     * Soft-delete a health center by setting isActive = false.
     *
     * This keeps historical data intact while preventing new references.
     *
     * @param id the health center ID to deactivate
     * @return the deactivated health center as a response DTO
     * @throws EntityNotFoundException if no health center matches the ID
     */
    @Transactional
    fun deactivateHealthCenter(id: UUID): HealthCenterResponse {
        val healthCenter = healthCenterRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Health center not found with id: $id") }

        if (!healthCenter.isActive) {
            logger.warn("Health center id={} is already inactive", id)
            return healthCenter.toResponse()
        }

        healthCenter.isActive = false
        val saved = healthCenterRepository.save(healthCenter)
        logger.info("Deactivated health center: id={}, name={}", saved.id, saved.name)
        return saved.toResponse()
    }

    fun getById(id: UUID): HealthCenterResponse {
        val hc = healthCenterRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Health center not found with id: $id") }
        return hc.toResponse()
    }

    @Transactional
    fun updateHealthCenter(id: UUID, request: UpdateHealthCenterRequest): HealthCenterResponse {
        val hc = healthCenterRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Health center not found with id: $id") }

        request.name?.let { newName ->
            val newSlug = generateSlug(newName)
            if (newSlug != hc.slug && healthCenterRepository.existsBySlug(newSlug)) {
                throw IllegalStateException("A health center with name '$newName' already exists")
            }
            hc.name = newName
            hc.slug = newSlug
        }

        request.department?.let { hc.department = it }

        val saved = healthCenterRepository.save(hc)
        logger.info("Updated health center: id={}, name={}, slug={}", saved.id, saved.name, saved.slug)
        return saved.toResponse()
    }

    @Transactional
    fun reactivateHealthCenter(id: UUID): HealthCenterResponse {
        val hc = healthCenterRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Health center not found with id: $id") }

        if (hc.isActive) {
            throw IllegalStateException("Health center is already active")
        }

        hc.isActive = true
        val saved = healthCenterRepository.save(hc)
        logger.info("Reactivated health center: id={}, name={}", saved.id, saved.name)
        return saved.toResponse()
    }

    /**
     * Generate a URL-friendly slug from a name string.
     *
     * Handles edge cases:
     * - Accented characters are normalized (á → a, ñ → n)
     * - Multiple spaces become a single hyphen
     * - Special characters are removed
     * - Consecutive hyphens are collapsed
     * - Leading/trailing hyphens are trimmed
     *
     * @param name the display name to convert
     * @return a unique, URL-safe slug string
     */
    private fun generateSlug(name: String): String {
        return Normalizer.normalize(name, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "-")
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-{2,}"), "-")
            .trim('-')
    }

    private fun HealthCenter.toResponse(): HealthCenterResponse = HealthCenterResponse(
        id = id!!,
        name = name,
        slug = slug,
        department = department,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
