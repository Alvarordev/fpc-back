package com.hazardev.fpc_back.healthcenter.application

import com.hazardev.fpc_back.healthcenter.application.dto.CreateHealthCenterRequest
import com.hazardev.fpc_back.healthcenter.application.dto.HealthCenterResponse
import com.hazardev.fpc_back.healthcenter.domain.HealthCenter
import com.hazardev.fpc_back.healthcenter.infrastructure.HealthCenterRepository
import com.hazardev.fpc_back.shared.domain.PeruDepartment
import jakarta.persistence.EntityNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.text.Normalizer

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

    /**
     * Get all active health centers.
     *
     * @return list of active health center response DTOs
     */
    fun getAllHealthCenters(): List<HealthCenterResponse> {
        return healthCenterRepository.findByIsActiveTrue()
            .map { it.toResponse() }
    }

    /**
     * Find a health center by its unique slug.
     *
     * @param slug the unique slug identifier
     * @return the health center as a response DTO
     * @throws EntityNotFoundException if no health center matches the slug
     */
    fun getBySlug(slug: String): HealthCenterResponse {
        val healthCenter = healthCenterRepository.findBySlug(slug)
            ?: throw EntityNotFoundException("Health center not found with slug: $slug")
        return healthCenter.toResponse()
    }

    /**
     * Find all health centers in a given department.
     *
     * @param department the Peru department to filter by
     * @return list of health center response DTOs in that department
     */
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
    fun deactivateHealthCenter(id: Long): HealthCenterResponse {
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

    /**
     * Map entity to response DTO.
     */
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
