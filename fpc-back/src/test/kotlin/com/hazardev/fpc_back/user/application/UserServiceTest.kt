package com.hazardev.fpc_back.user.application

import com.hazardev.fpc_back.user.application.dto.CreateUserRequest
import com.hazardev.fpc_back.user.application.dto.UpdateUserRequest
import com.hazardev.fpc_back.user.domain.User
import com.hazardev.fpc_back.user.domain.UserRole
import com.hazardev.fpc_back.user.infrastructure.UserRepository
import jakarta.persistence.EntityNotFoundException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @Mock
    private lateinit var passwordEncoder: PasswordEncoder

    @InjectMocks
    private lateinit var userService: UserService

    @Test
    fun `getAllUsers should return paginated users`() {
        val users = listOf(
            User(id = UUID.randomUUID(), email = "a@test.com", passwordHash = "h", role = UserRole.ADMIN),
            User(id = UUID.randomUUID(), email = "b@test.com", passwordHash = "h", role = UserRole.AGENT)
        )
        whenever(userRepository.findAll(PageRequest.of(0, 10))).thenReturn(PageImpl(users))

        val result = userService.getAllUsers(PageRequest.of(0, 10))

        assertThat(result.content).hasSize(2)
        assertThat(result.content[0].email).isEqualTo("a@test.com")
    }

    @Test
    fun `getUserById should return user when found`() {
        val id = UUID.randomUUID()
        val user = User(id = id, email = "test@test.com", passwordHash = "h", role = UserRole.PSYCHOLOGIST)
        whenever(userRepository.findById(id)).thenReturn(Optional.of(user))

        val result = userService.getUserById(id)

        assertThat(result.email).isEqualTo("test@test.com")
        assertThat(result.role).isEqualTo(UserRole.PSYCHOLOGIST)
    }

    @Test
    fun `getUserById should throw when not found`() {
        val id = UUID.randomUUID()
        whenever(userRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
            userService.getUserById(id)
        }
    }

    @Test
    fun `createUser should hash password and save`() {
        val request = CreateUserRequest("new@test.com", "password", UserRole.AGENT)
        whenever(userRepository.existsByEmail("new@test.com")).thenReturn(false)
        whenever(passwordEncoder.encode("password")).thenReturn("encoded")
        val savedUser = User(
            id = UUID.randomUUID(),
            email = "new@test.com",
            passwordHash = "encoded",
            role = UserRole.AGENT
        )
        whenever(userRepository.save(any<User>())).thenReturn(savedUser)

        val result = userService.createUser(request)

        assertThat(result.email).isEqualTo("new@test.com")
        assertThat(result.role).isEqualTo(UserRole.AGENT)
    }

    @Test
    fun `createUser should throw when email exists`() {
        val request = CreateUserRequest("existing@test.com", "password", UserRole.AGENT)
        whenever(userRepository.existsByEmail("existing@test.com")).thenReturn(true)

        assertThrows<IllegalArgumentException> {
            userService.createUser(request)
        }
    }

    @Test
    fun `updateUser should update fields`() {
        val id = UUID.randomUUID()
        val user = User(id = id, email = "old@test.com", passwordHash = "old", role = UserRole.AGENT)
        whenever(userRepository.findById(id)).thenReturn(Optional.of(user))
        whenever(passwordEncoder.encode("newpass")).thenReturn("newhash")
        whenever(userRepository.save(any<User>())).thenAnswer { it.arguments[0] as User }

        val result = userService.updateUser(id, UpdateUserRequest(email = "new@test.com", password = "newpass", role = UserRole.ADMIN, isActive = false))

        assertThat(result.email).isEqualTo("new@test.com")
        assertThat(result.role).isEqualTo(UserRole.ADMIN)
        assertThat(result.isActive).isFalse()
    }

    @Test
    fun `updateUser should throw when user not found`() {
        val id = UUID.randomUUID()
        whenever(userRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
            userService.updateUser(id, UpdateUserRequest(email = "new@test.com"))
        }
    }

    @Test
    fun `deleteUser should delete existing user`() {
        val id = UUID.randomUUID()
        whenever(userRepository.existsById(id)).thenReturn(true)

        userService.deleteUser(id)
    }

    @Test
    fun `deleteUser should throw when user not found`() {
        val id = UUID.randomUUID()
        whenever(userRepository.existsById(id)).thenReturn(false)

        assertThrows<EntityNotFoundException> {
            userService.deleteUser(id)
        }
    }
}
