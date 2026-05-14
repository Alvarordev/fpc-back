package com.hazardev.fpc_back.volunteer.application

import com.hazardev.fpc_back.user.infrastructure.UserRepository
import com.hazardev.fpc_back.volunteer.application.dto.CreateVolunteerRequest
import com.hazardev.fpc_back.volunteer.application.dto.UpdateVolunteerRequest
import com.hazardev.fpc_back.volunteer.application.dto.VolunteerResponse
import com.hazardev.fpc_back.volunteer.domain.Volunteer
import com.hazardev.fpc_back.volunteer.infrastructure.VolunteerRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class VolunteerService(
    private val volunteerRepository: VolunteerRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun create(request: CreateVolunteerRequest): VolunteerResponse {
        if (volunteerRepository.existsByUserId(request.userId)) {
            throw IllegalStateException("User is already linked to a volunteer")
        }

        val user = userRepository.findById(request.userId)
            .orElseThrow { EntityNotFoundException("User not found") }

        val volunteer = Volunteer(
            user = user,
            firstName = request.firstName,
            lastName = request.lastName,
            specialty = request.specialty,
            email = request.email,
            phone = request.phone,
            isActive = request.isActive
        )

        return volunteerRepository.saveAndFlush(volunteer).toResponse()
    }

    fun getAll(): List<VolunteerResponse> {
        return volunteerRepository.findAll().map { it.toResponse() }
    }

    fun getById(id: UUID): VolunteerResponse {
        val volunteer = volunteerRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Volunteer not found with id: $id") }
        return volunteer.toResponse()
    }

    fun getByUserId(userId: UUID): VolunteerResponse {
        val volunteer = volunteerRepository.findByUserId(userId)
            ?: throw EntityNotFoundException("Volunteer not found for user id: $userId")
        return volunteer.toResponse()
    }

    @Transactional
    fun update(id: UUID, request: UpdateVolunteerRequest): VolunteerResponse {
        val volunteer = volunteerRepository.findById(id)
            .orElseThrow { EntityNotFoundException("Volunteer not found with id: $id") }

        request.firstName?.let { volunteer.firstName = it }
        request.lastName?.let { volunteer.lastName = it }
        request.specialty?.let { volunteer.specialty = it }
        request.email?.let { volunteer.email = it }
        request.phone?.let { volunteer.phone = it }
        request.isActive?.let { volunteer.isActive = it }

        return volunteerRepository.saveAndFlush(volunteer).toResponse()
    }

    @Transactional
    fun delete(id: UUID) {
        if (!volunteerRepository.existsById(id)) {
            throw EntityNotFoundException("Volunteer not found with id: $id")
        }
        volunteerRepository.deleteById(id)
    }

    private fun Volunteer.toResponse(): VolunteerResponse = VolunteerResponse(
        id = id ?: throw IllegalStateException("Volunteer ID is null after save"),
        userId = user.id ?: throw IllegalStateException("User ID is null on volunteer"),
        firstName = firstName,
        lastName = lastName,
        specialty = specialty,
        email = email,
        phone = phone,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}
