package com.hazardev.fpc_back.user.api

import com.hazardev.fpc_back.user.application.dto.CreateUserRequest
import com.hazardev.fpc_back.user.application.dto.LoginRequest
import com.hazardev.fpc_back.user.application.dto.TokenResponse
import com.hazardev.fpc_back.user.application.dto.UpdateUserRequest
import com.hazardev.fpc_back.user.application.dto.UserResponse
import com.hazardev.fpc_back.user.domain.User
import com.hazardev.fpc_back.user.domain.UserRole
import com.hazardev.fpc_back.user.infrastructure.UserRepository
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
class UserControllerIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val createdUserIds = mutableListOf<UUID>()

    @AfterEach
    fun cleanup() {
        createdUserIds.forEach { id ->
            if (userRepository.existsById(id)) {
                userRepository.deleteById(id)
            }
        }
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

    @Test
    fun `get users without token returns 401`() {
        val response = restTemplate.getForEntity("/users", String::class.java)
        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `get users with valid token returns 200`() {
        val token = getAdminToken()
        val headers = authHeaders(token)
        val entity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange("/users", HttpMethod.GET, entity, String::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
    }

    @Test
    fun `create user as admin returns 201`() {
        val token = getAdminToken()
        val headers = authHeaders(token)
        val request = CreateUserRequest("new-${UUID.randomUUID()}@test.com", "password", UserRole.AGENT)
        val entity = HttpEntity(request, headers)
        val response = restTemplate.exchange("/users", HttpMethod.POST, entity, UserResponse::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.CREATED)
        assertThat(response.body?.email).isEqualTo(request.email)
        assertThat(response.body?.role).isEqualTo(UserRole.AGENT)

        response.body?.id?.let { createdUserIds.add(it) }
    }

    @Test
    fun `create user as agent returns 403`() {
        val token = getAgentToken()
        val headers = authHeaders(token)
        val request = CreateUserRequest("another-${UUID.randomUUID()}@test.com", "password", UserRole.AGENT)
        val entity = HttpEntity(request, headers)
        val response = restTemplate.exchange("/users", HttpMethod.POST, entity, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }

    @Test
    fun `get user by id returns user`() {
        val token = getAdminToken()
        val headers = authHeaders(token)

        // Create a user first
        val createRequest = CreateUserRequest("get-${UUID.randomUUID()}@test.com", "password", UserRole.PSYCHOLOGIST)
        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.exchange("/users", HttpMethod.POST, createEntity, UserResponse::class.java)
        val userId = createResponse.body!!.id
        createdUserIds.add(userId)

        // Get the user
        val getEntity = HttpEntity<Void>(headers)
        val getResponse = restTemplate.exchange("/users/$userId", HttpMethod.GET, getEntity, UserResponse::class.java)

        assertThat(getResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(getResponse.body?.id).isEqualTo(userId)
        assertThat(getResponse.body?.email).isEqualTo(createRequest.email)
    }

    @Test
    fun `update user as admin returns updated user`() {
        val token = getAdminToken()
        val headers = authHeaders(token)

        // Create a user first
        val createRequest = CreateUserRequest("update-${UUID.randomUUID()}@test.com", "password", UserRole.AGENT)
        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.exchange("/users", HttpMethod.POST, createEntity, UserResponse::class.java)
        val userId = createResponse.body!!.id
        createdUserIds.add(userId)

        // Update the user
        val updateRequest = UpdateUserRequest(role = UserRole.PSYCHOLOGIST, isActive = false)
        val updateEntity = HttpEntity(updateRequest, headers)
        val updateResponse = restTemplate.exchange("/users/$userId", HttpMethod.PUT, updateEntity, UserResponse::class.java)

        assertThat(updateResponse.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(updateResponse.body?.role).isEqualTo(UserRole.PSYCHOLOGIST)
        assertThat(updateResponse.body?.isActive).isFalse()
    }

    @Test
    fun `delete user as admin returns 204`() {
        val token = getAdminToken()
        val headers = authHeaders(token)

        // Create a user first
        val createRequest = CreateUserRequest("delete-${UUID.randomUUID()}@test.com", "password", UserRole.AGENT)
        val createEntity = HttpEntity(createRequest, headers)
        val createResponse = restTemplate.exchange("/users", HttpMethod.POST, createEntity, UserResponse::class.java)
        val userId = createResponse.body!!.id
        createdUserIds.add(userId)

        // Delete the user
        val deleteEntity = HttpEntity<Void>(headers)
        val deleteResponse = restTemplate.exchange("/users/$userId", HttpMethod.DELETE, deleteEntity, Void::class.java)

        assertThat(deleteResponse.statusCode).isEqualTo(HttpStatus.NO_CONTENT)
        createdUserIds.remove(userId)
    }

    @Test
    fun `delete user as agent returns 403`() {
        val token = getAgentToken()
        val headers = authHeaders(token)
        val deleteEntity = HttpEntity<Void>(headers)
        val response = restTemplate.exchange("/users/${UUID.randomUUID()}", HttpMethod.DELETE, deleteEntity, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.FORBIDDEN)
    }
}
