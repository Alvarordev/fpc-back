package com.hazardev.fpc_back.patient.infrastructure.gemini

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.kotlin.mock
import org.springframework.jdbc.core.JdbcTemplate
import kotlin.test.assertEquals

class GeminiClientTest {

    private val jdbcTemplate: JdbcTemplate = mock()
    private val geminiClient = GeminiClient(
        apiKey = "dummy-key",
        jdbcTemplate = jdbcTemplate
    )

    @Test
    fun `should throw exception for non-SELECT queries`() {
        val exception = assertThrows<IllegalArgumentException> {
            geminiClient.executeSql("UPDATE patients SET status = 'ACTIVE' WHERE dni = '12345678'")
        }
        assertEquals("Only SELECT queries are allowed.", exception.message)
    }

    @Test
    fun `should throw exception when forbidden keywords are present`() {
        val queries = listOf(
            "SELECT * FROM patients; DROP TABLE patients;",
            "SELECT * FROM patients WHERE id IN (SELECT id FROM patients); DELETE FROM patients;",
            "SELECT * FROM patients WHERE name = 'John' UNION SELECT * FROM users; UPDATE users SET is_active = true;"
        )
        
        for (query in queries) {
            val exception = assertThrows<IllegalArgumentException>("Expected query to be rejected: $query") {
                geminiClient.executeSql(query)
            }
            assert(exception.message!!.contains("Forbidden keyword") || exception.message == "Only SELECT queries are allowed.")
        }
    }

}

