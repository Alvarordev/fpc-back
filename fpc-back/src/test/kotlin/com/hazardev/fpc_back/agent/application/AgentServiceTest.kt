package com.hazardev.fpc_back.agent.application

import com.hazardev.fpc_back.agent.application.dto.CreateAgentRequest
import com.hazardev.fpc_back.agent.application.dto.UpdateAgentRequest
import com.hazardev.fpc_back.agent.domain.Agent
import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
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
import java.time.Instant
import java.util.Optional
import java.util.UUID

@ExtendWith(MockitoExtension::class)
class AgentServiceTest {

    @Mock
    private lateinit var agentRepository: AgentRepository

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var agentService: AgentService

    @Test
    fun `createAgent should create when user exists and free`() {
        val userId = UUID.randomUUID()
        val user = User(id = userId, email = "agent@test.com", passwordHash = "hash", role = UserRole.AGENT)
        val request = CreateAgentRequest(userId = userId, fullName = "Agent One", phone = "+111")

        whenever(agentRepository.existsByUserId(userId)).thenReturn(false)
        whenever(userRepository.findById(userId)).thenReturn(Optional.of(user))
        whenever(agentRepository.save(any<Agent>())).thenAnswer { invocation ->
            val a = invocation.arguments[0] as Agent
            Agent(id = UUID.randomUUID(), user = a.user, fullName = a.fullName, phone = a.phone, createdAt = Instant.now())
        }

        val result = agentService.createAgent(request)

        assertThat(result.userId).isEqualTo(userId)
        assertThat(result.fullName).isEqualTo("Agent One")
    }

    @Test
    fun `createAgent should throw when user already linked`() {
        val userId = UUID.randomUUID()
        whenever(agentRepository.existsByUserId(userId)).thenReturn(true)

        assertThrows<IllegalStateException> {
            agentService.createAgent(CreateAgentRequest(userId = userId, fullName = "X", phone = "Y"))
        }
    }

    @Test
    fun `getAgentById should throw when missing`() {
        val id = UUID.randomUUID()
        whenever(agentRepository.findById(id)).thenReturn(Optional.empty())

        assertThrows<EntityNotFoundException> {
            agentService.getAgentById(id)
        }
    }

    @Test
    fun `updateAgent should update basic fields`() {
        val userId = UUID.randomUUID()
        val agentId = UUID.randomUUID()
        val user = User(id = userId, email = "agent@test.com", passwordHash = "hash", role = UserRole.AGENT)
        val agent = Agent(id = agentId, user = user, fullName = "Old Name", phone = "123", createdAt = Instant.now())

        whenever(agentRepository.findById(agentId)).thenReturn(Optional.of(agent))
        whenever(agentRepository.save(any<Agent>())).thenAnswer { it.arguments[0] as Agent }

        val result = agentService.updateAgent(agentId, UpdateAgentRequest(fullName = "New Name", phone = "456"))

        assertThat(result.fullName).isEqualTo("New Name")
        assertThat(result.phone).isEqualTo("456")
    }
}
