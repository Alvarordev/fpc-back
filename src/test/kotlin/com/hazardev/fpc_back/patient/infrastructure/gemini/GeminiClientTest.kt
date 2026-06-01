package com.hazardev.fpc_back.patient.infrastructure.gemini

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class GeminiClientTest {

    @Test
    fun `should throw exception when API key is blank`() {
        val client = GeminiClient(apiKey = "  ")
        val exception = assertThrows<IllegalStateException> {
            client.generatePatientSummary("{}")
        }
        assertEquals(
            "La API Key de Gemini no está configurada. Por favor, define la variable de entorno GEMINI_API_KEY.",
            exception.message
        )
    }

    @Test
    fun `should accept valid API key without error in construction`() {
        val client = GeminiClient(apiKey = "test-api-key-12345")
        // Construction succeeds without jdbcTemplate
        // Actual API call would require network, so we only verify construction
        kotlin.test.assertNotNull(client)
    }
}
