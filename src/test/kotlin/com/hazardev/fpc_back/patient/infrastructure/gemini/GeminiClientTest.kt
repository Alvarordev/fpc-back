package com.hazardev.fpc_back.patient.infrastructure.gemini

import tools.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GeminiClientTest {
    private val objectMapper = ObjectMapper()

    @Test
    fun `should throw exception when API key is blank`() {
        val client = GeminiClient(apiKey = "  ", objectMapper = objectMapper)
        val exception = assertThrows<PatientSummaryGenerationException> {
            client.generatePatientSummary("{}")
        }
        assertEquals(
            "La API Key de Gemini no esta configurada. Por favor, define la variable de entorno GEMINI_API_KEY.",
            exception.message
        )
    }

    @Test
    fun `should accept valid API key without error in construction`() {
        val client = GeminiClient(apiKey = "test-api-key-12345", objectMapper = objectMapper)
        assertNotNull(client)
    }

    @Test
    fun `should clean fenced json response`() {
        val client = GeminiClient(apiKey = "test-api-key-12345", objectMapper = objectMapper)

        val cleaned = client.validateAndCleanJson("```json\n{\"ok\":true}\n```")

        assertEquals("{\"ok\":true}", cleaned)
    }

    @Test
    fun `should reject invalid json`() {
        val client = GeminiClient(apiKey = "test-api-key-12345", objectMapper = objectMapper)

        assertFailsWith<PatientSummaryGenerationException> {
            client.validateAndCleanJson("not-json")
        }
    }
}
