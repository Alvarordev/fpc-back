package com.hazardev.fpc_back.patient.infrastructure.gemini

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GeminiClient(
    @Value("\${gemini.api.key}") private val apiKey: String
) {
    private val logger = LoggerFactory.getLogger(GeminiClient::class.java)
    private val restClient = RestClient.builder().build()

    data class GeminiRequest(
        val contents: List<Content>,
        val systemInstruction: SystemInstruction? = null,
        val generationConfig: GenerationConfig? = null
    )

    data class Content(
        val role: String? = null,
        val parts: List<Part>? = null
    )

    data class Part(
        val text: String? = null
    )

    data class SystemInstruction(
        val parts: List<Part>? = null
    )

    data class GenerationConfig(
        val responseMimeType: String? = null,
        val temperature: Double? = null
    )

    data class GeminiResponse(
        val candidates: List<Candidate>? = null
    )

    data class Candidate(
        val content: Content? = null,
        val finishReason: String? = null
    )

    fun generatePatientSummary(patientDataJson: String): String {
        if (apiKey.isBlank()) {
            throw IllegalStateException("La API Key de Gemini no está configurada. Por favor, define la variable de entorno GEMINI_API_KEY.")
        }

        val systemPrompt = """
            Eres un asistente médico experto para la aplicación FPC (Fundación Peruana de Cáncer).
            Tu tarea es generar un resumen detallado de un paciente en formato JSON a partir de los datos que se te proporcionan.

            A continuación recibirás todos los datos disponibles del paciente en formato JSON.
            Debes analizar esta información y producir un resumen estructurado ÚNICAMENTE en el siguiente formato JSON en español:

            {
              "datosPersonales": {
                "nombreCompleto": "...",
                "dni": "...",
                "fechaNacimiento": "...",
                "genero": "...",
                "telefono": "...",
                "tieneWhatsapp": false,
                "estado": "..."
              },
              "contactoEmergencia": {
                "nombre": "...",
                "telefono": "..."
              },
              "seguro": {
                "tipo": "...",
                "proveedorEps": "...",
                "esVigente": false
              },
              "diagnosticos": [
                {
                  "diagnostico": "...",
                  "etapa": "...",
                  "fecha": "...",
                  "centroSalud": "...",
                  "esActual": false
                }
              ],
              "tratamientos": [
                {
                  "tipo": "...",
                  "frecuencia": "...",
                  "situacion": "...",
                  "fechaInicio": "...",
                  "esActual": false
                }
              ],
              "proximasCitas": [
                {
                  "especialidad": "...",
                  "fechaCita": "...",
                  "fechaProximaCita": "...",
                  "dificultades": "..."
                }
              ],
              "sintomas": [
                {
                  "severidad": "...",
                  "descripcion": "...",
                  "tieneDolor": false,
                  "intensidadDolor": 0,
                  "ubicacionDolor": "..."
                }
              ],
              "resumenEjecutivo": "Un resumen ejecutivo clínico redactado por la IA sobre la condición del paciente, estado de afiliación, tratamientos vigentes y alertas o necesidades identificadas."
            }

            Reglas críticas:
            - NO inventes información. Si un campo no tiene datos, usa null o array vacío [].
            - El campo "resumenEjecutivo" debe ser un texto en español de 3-5 oraciones que resuma la condición del paciente, tratamientos activos, estado de seguro/afiliación, y cualquier alerta o necesidad urgente identificada.
            - Para "seguro", prioriza el registro con esVigente=true si existe. Si no hay ninguno vigente, usa el más reciente.
            - Devuelve EXCLUSIVAMENTE el JSON, sin texto antes ni después, sin markdown, sin explicaciones.
            - Responde SIEMPRE en español.
        """.trimIndent()

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"

        val userPrompt = "A continuación están todos los datos del paciente en formato JSON. Genera el resumen estructurado siguiendo las instrucciones del sistema.\n\n$patientDataJson"

        val requestBody = GeminiRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = userPrompt))
                )
            ),
            systemInstruction = SystemInstruction(parts = listOf(Part(text = systemPrompt))),
            generationConfig = GenerationConfig(
                temperature = 0.2
            )
        )

        logger.info("Calling Gemini API for patient summary (single call, pre-fetched data)...")
        val geminiResponse = try {
            restClient.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(GeminiResponse::class.java)
        } catch (e: Exception) {
            logger.error("Error calling Gemini API: ", e)
            throw RuntimeException("Error al comunicarse con la IA de Gemini: ${e.message}", e)
        }

        val text = geminiResponse?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: throw RuntimeException("La respuesta de Gemini no contiene texto. FinishReason: ${geminiResponse?.candidates?.firstOrNull()?.finishReason}")

        return cleanJson(text)
    }

    private fun cleanJson(input: String): String {
        var text = input.trim()
        if (text.startsWith("```json")) {
            text = text.substring(7)
        } else if (text.startsWith("```")) {
            text = text.substring(3)
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length - 3)
        }
        return text.trim()
    }
}
