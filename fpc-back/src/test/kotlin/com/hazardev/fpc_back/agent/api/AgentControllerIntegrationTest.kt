package com.hazardev.fpc_back.agent.api

import com.hazardev.fpc_back.agent.application.dto.AgentResponse
import com.hazardev.fpc_back.agent.application.dto.CreateAgentRequest
import com.hazardev.fpc_back.user.application.dto.LoginRequest
import com.hazardev.fpc_back.user.application.dto.TokenResponse
import com.hazardev.fpc_back.user.domain.User
import com.hazardev.fpc_back.user.domain.UserRole
import com.hazardev.fpc_back.user.infrastructure.UserRepository
import com.hazardev.fpc_back.agent.infrastructure.AgentRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.crypto.password.PasswordEncoder
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AgentControllerIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var agentRepository: AgentRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val createdAgentIds = mutableListOf<UUID>()
    private val createdUserIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanup() {
        createdAgentIds.forEach { id ->
            if (agentRepository.existsById(id)) {
                agentRepository.deleteById(id)
            }
        }
        createdUserIds.forEach { id ->
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id)
            }
        }
        createdAgentIds.clear()
        createdUserIds.clear()
    }

    private fun getAdminToken(): String {
        val request = LoginRequest("admin@gmail.com", "123456")
        val response = restTemplate.postForEntity("/auth/login", request, TokenResponse::class.java)
        return response.body!!.accessToken
    }

    private fun getAgentToken(): String {
        val email = "agent-${UUID.randomUUID()}@test.com"
        val agent = User(
            email = email,
            passwordHash = passwordEncoder.encode("password")!!,
            role = UserRole.AGENT
        )
        val saved = userRepository.save(agent)
        createdUserIds.add(saved.id!!)

        val request = LoginRequest(email, "password")
        val response = restTemplate.postForEntity("/auth/login", request, TokenResponse::class.java)
        return response.body!!.accessToken
    }

    private fun authHeaders(token: String): HttpHeaders {
        return HttpHeaders().apply {
            setBearerAuth(token)
            contentType = MediaType.APPLICATION_JSON
        }
    }

    private fun createUserForAgent(): UUID {
        val user = User(
            email = "agent-user-${UUID.randomUUID()}@test.com",
            passwordHash = passwordEncoder.encode("password")!!,
            role = UserRole.AGENT
        )
        val saved = userRepository.save(user)
        createdUserIds.add(saved.id!!)
        return saved.id!!
    }

    @Test
    fun `get agents without token returns 401`() {
        val response = restTemplate.getForEntity("/agents", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `create agent as admin returns 201`() {
        val token = getAdminToken()
        val headers = authHeaders(token)
        val userId = createUserForAgent()

        val request = CreateAgentRequest(userId = userId, fullName = "Agent Integration", phone = "+123456")
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange("/agents", HttpMethod.POST, entity, AgentResponse::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.userId).isEqualTo(userId)
        response.body?.id?.let { createdAgentIds.add(it) }
    }

    @Test
    fun `create agent as agent returns 403`() {
        val token = getAgentToken()
        val headers = authHeaders(token)
        val userId = createUserForAgent()

        val request = CreateAgentRequest(userId = userId, fullName = "No Access", phone = "+999")
        val entity = HttpEntity(request, headers)

        val response = restTemplate.exchange("/agents", HttpMethod.POST, entity, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `create agent with same user twice returns 400`() {
        val token = getAdminToken()
        val headers = authHeaders(token)
        val userId = createUserForAgent()

        val request = CreateAgentRequest(userId = userId, fullName = "Unique User", phone = "+1010")
        val entity = HttpEntity(request, headers)

        val first = restTemplate.exchange("/agents", HttpMethod.POST, entity, AgentResponse::class.java)
        createdAgentIds.add(first.body!!.id)

        val second = restTemplate.exchange("/agents", HttpMethod.POST, entity, Map::class.java)
        assertThat(second.statusCode).isEqualTo(HttpStatus.BAD_REQUEST)
    }
}
