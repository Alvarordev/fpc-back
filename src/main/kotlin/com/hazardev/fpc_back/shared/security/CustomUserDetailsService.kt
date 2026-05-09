package com.hazardev.fpc_back.shared.security

import com.hazardev.fpc_back.user.infrastructure.UserRepository
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class CustomUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByEmail(username)
            ?: throw UsernameNotFoundException("User not found: $username")

        return User.builder()
            .username(user.email)
            .password(user.passwordHash)
            .authorities("ROLE_${user.role.name}")
            .disabled(!user.isActive)
            .build()
    }
}
