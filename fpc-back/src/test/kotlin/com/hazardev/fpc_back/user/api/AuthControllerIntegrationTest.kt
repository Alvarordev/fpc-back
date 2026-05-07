package com.hazardev.fpc_back.user.api

import com.hazardev.fpc_back.user.application.dto.LoginRequest
import com.hazardev.fpc_back.user.application.dto.RefreshRequest
import com.hazardev.fpc_back.user.application.dto.TokenResponse
import com.hazardev.fpc_back.user.infrastructure.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerIntegrationTest {

    @Autowired
    private lateinit var restTemplate: TestRestTemplate

    @Autowired
    private lateinit var userRepository: UserRepository

    private val createdUserEmails = mutableListOf<String>()

    @AfterEach
    fun cleanup() {
        createdUserEmails.forEach { email ->
            userRepository.findByEmail(email)?.let { userRepository.delete(it) }
        }
        createdUserEmails.clear()
    }

    @Test
    fun `login with valid credentials returns tokens`() {
        val request = LoginRequest("admin@gmail.com", "123456")
        val response = restTemplate.postForEntity("/auth/login", request, TokenResponse::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.accessToken).isNotBlank()
        assertThat(response.body?.refreshToken).isNotBlank()
        assertThat(response.body?.tokenType).isEqualTo("Bearer")
    }

    @Test
    fun `login with invalid password returns 401`() {
        val request = LoginRequest("admin@gmail.com", "wrongpassword")
        val response = restTemplate.postForEntity("/auth/login", request, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `login with nonexistent user returns 401`() {
        val request = LoginRequest("nonexistent@test.com", "password")
        val response = restTemplate.postForEntity("/auth/login", request, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }

    @Test
    fun `refresh with valid refresh token returns new tokens`() {
        val loginRequest = LoginRequest("admin@gmail.com", "123456")
        val loginResponse = restTemplate.postForEntity("/auth/login", loginRequest, TokenResponse::class.java)
        val refreshToken = loginResponse.body!!.refreshToken

        val refreshRequest = RefreshRequest(refreshToken)
        val response = restTemplate.postForEntity("/auth/refresh", refreshRequest, TokenResponse::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK)
        assertThat(response.body?.accessToken).isNotBlank()
        assertThat(response.body?.refreshToken).isNotBlank()
    }

    @Test
    fun `refresh with invalid token returns 401`() {
        val request = RefreshRequest("invalid-token")
        val response = restTemplate.postForEntity("/auth/refresh", request, Map::class.java)

        assertThat(response.statusCode).isEqualTo(HttpStatus.UNAUTHORIZED)
    }
}
