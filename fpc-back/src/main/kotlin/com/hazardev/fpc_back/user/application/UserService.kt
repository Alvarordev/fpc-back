package com.hazardev.fpc_back.user.application

import com.hazardev.fpc_back.user.application.dto.CreateUserRequest
import com.hazardev.fpc_back.user.application.dto.UpdateUserRequest
import com.hazardev.fpc_back.user.application.dto.UserResponse
import com.hazardev.fpc_back.user.domain.User
import com.hazardev.fpc_back.user.infrastructure.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder
) {

    fun getAllUsers(pageable: Pageable): Page<UserResponse> {
        return userRepository.findAll(pageable).map { it.toResponse() }
    }

    fun getUserById(id: UUID): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User not found") }
        return user.toResponse()
    }

    @Transactional
    fun createUser(request: CreateUserRequest): UserResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email already exists")
        }

        val user = User(
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)!!,
            role = request.role,
            isActive = true
        )

        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun updateUser(id: UUID, request: UpdateUserRequest): UserResponse {
        val user = userRepository.findById(id)
            .orElseThrow { EntityNotFoundException("User not found") }

        request.email?.let {
            if (it != user.email && userRepository.existsByEmail(it)) {
                throw IllegalArgumentException("Email already exists")
            }
            user.email = it
        }
        request.password?.let { user.passwordHash = passwordEncoder.encode(it)!! }
        request.role?.let { user.role = it }
        request.isActive?.let { user.isActive = it }

        return userRepository.save(user).toResponse()
    }

    @Transactional
    fun deleteUser(id: UUID) {
        if (!userRepository.existsById(id)) {
            throw EntityNotFoundException("User not found")
        }
        userRepository.deleteById(id)
    }

    private fun User.toResponse(): UserResponse = UserResponse(
        id = id!!,
        email = email,
        role = role,
        isActive = isActive,
        createdAt = createdAt!!,
        updatedAt = updatedAt!!
    )
}
