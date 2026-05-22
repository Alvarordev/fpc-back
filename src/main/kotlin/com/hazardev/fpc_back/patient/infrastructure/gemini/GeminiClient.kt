package com.hazardev.fpc_back.patient.infrastructure.gemini

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

@Component
class GeminiClient(
    @Value("\${gemini.api.key}") private val apiKey: String,
    private val jdbcTemplate: JdbcTemplate
) {
    private val logger = LoggerFactory.getLogger(GeminiClient::class.java)
    private val restClient = RestClient.builder().build()

    // Gemini API Request and Response DTOs
    data class GeminiRequest(
        val contents: List<Content>,
        val tools: List<Tool>? = null,
        val systemInstruction: SystemInstruction? = null,
        val generationConfig: GenerationConfig? = null
    )

    data class Content(
        val role: String? = null,
        val parts: List<Part>? = null
    )

    data class Part(
        val text: String? = null,
        val functionCall: FunctionCall? = null,
        val functionResponse: FunctionResponse? = null
    )

    data class Tool(
        val functionDeclarations: List<FunctionDeclaration>
    )

    data class FunctionDeclaration(
        val name: String,
        val description: String,
        val parameters: Schema
    )

    data class Schema(
        val type: String,
        val properties: Map<String, SchemaProperty>? = null,
        val required: List<String>? = null
    )

    data class SchemaProperty(
        val type: String,
        val description: String
    )

    data class SystemInstruction(
        val parts: List<Part>? = null
    )

    data class GenerationConfig(
        val responseMimeType: String? = null,
        val temperature: Double? = null
    )

    data class FunctionCall(
        val name: String? = null,
        val args: Map<String, Any?>? = null
    )
 
    data class FunctionResponse(
        val name: String? = null,
        val response: Map<String, Any?>? = null
    )
 
    data class GeminiResponse(
        val candidates: List<Candidate>? = null
    )
 
    data class Candidate(
        val content: Content? = null,
        val finishReason: String? = null
    )
 
    fun executeSql(query: String): List<Map<String, Any?>> {
        val trimmed = query.trim()
        
        // Basic read-only validation
        if (!trimmed.uppercase().startsWith("SELECT")) {
            throw IllegalArgumentException("Only SELECT queries are allowed.")
        }
        
        // Safety check to prevent write/destructive queries
        val forbiddenKeywords = listOf("INSERT", "UPDATE", "DELETE", "DROP", "ALTER", "TRUNCATE", "CREATE", "REPLACE", "GRANT", "REVOKE")
        for (keyword in forbiddenKeywords) {
            if (trimmed.uppercase().contains(Regex("\\b$keyword\\b"))) {
                throw IllegalArgumentException("SQL Execution Error: Forbidden keyword '$keyword' found in query.")
            }
        }
        
        logger.info("Executing safe read-only SQL for Gemini: $trimmed")
        return jdbcTemplate.queryForList(trimmed)
    }

    fun generatePatientSummary(dni: String): String {
        if (apiKey.isBlank()) {
            throw IllegalStateException("La API Key de Gemini no está configurada. Por favor, define la variable de entorno GEMINI_API_KEY.")
        }

        val systemPrompt = """
            Eres un asistente de base de datos experto para la aplicación FPC (Fundación Peruana de Cáncer).
            Tu tarea es generar un resumen detallado de un paciente en formato JSON utilizando la herramienta execute_sql para consultar la base de datos PostgreSQL.

            Las tablas relevantes y sus columnas son:
            1. patients: id, full_name, dni, birth_date, primary_phone, secondary_phone, has_whatsapp, role (UNKNOWN, PATIENT, COMPANION), status (PROSPECT, ENROLLED, ACTIVE, INACTIVE), gender.
            2. patient_details: patient_id, birth_department, current_address, current_district, current_department, emergency_contact_name, emergency_contact_phone, education_level, native_language, requires_translation, zone_type.
            3. patient_insurance: patient_id, insurance_type (SIS, ESSALUD, EPS, FUERZAS_ARMADAS, SALUDPOL, NONE), eps_provider, is_current, start_date, end_date.
            4. patient_diagnoses: patient_id, diagnosis, cancer_stage (STAGE_1, STAGE_2, STAGE_3, STAGE_4, UNKNOWN), diagnosis_date, diagnosis_specialty, health_center_id, is_current.
            5. patient_treatments: patient_id, treatment_type, treatment_frequency, start_date, end_date, is_current, treatment_situation.
            6. patient_medical_appointments: patient_id, specialty, appointment_date, next_appointment_date, difficulties, is_first_consultation, health_center_id.
            7. patient_sis_affiliation: patient_id, can_affiliate, affiliated_at, comments.
            8. enrollments: patient_id, currently_attending_consultations, currently_receiving_treatment, entry_source, entry_sub_source, requires_transportation, has_mobility_issues, is_oncological_patient.
            9. patient_symptom_reports: patient_id, discomfort_severity, discomfort_description, symptom_duration, symptom_frequency, is_pain_present, pain_intensity, pain_location, pain_description.
            10. contacts: patient_id, type (WHATSAPP, CALL, VIDEO_CALL, EMAIL, IN_PERSON), status (SCHEDULED, COMPLETED, CANCELLED, NO_ANSWER), purpose, completed_at, notes.
            11. health_centers: id, name, department.

            Reglas críticas de consulta SQL:
            - SOLO puedes ejecutar consultas SELECT. Cualquier otra operación de escritura o modificación está estrictamente prohibida.
            - Filtra siempre por el DNI provisto por el usuario o por el patient_id obtenido.
            - Primero busca el paciente en la tabla `patients` por su DNI. Por ejemplo: `SELECT id, full_name, dni, birth_date, gender, status FROM patients WHERE dni = '...'`
            - Con el patient_id obtenido, realiza consultas para las demás tablas según necesites para el resumen.
            - Limita siempre los resultados (por ejemplo, `LIMIT 1` para registros únicos o `LIMIT 10` para listas) para evitar sobrecargar la memoria.
            - Devuelve la respuesta final en un formato JSON estructurado válido en español con la siguiente estructura:
              {
                "datosPersonales": { "nombreCompleto", "dni", "fechaNacimiento", "genero", "telefono", "tieneWhatsapp", "estado" },
                "contactoEmergencia": { "nombre", "telefono" },
                "seguro": { "tipo", "proveedorEps", "esVigente" },
                "diagnosticos": [ { "diagnostico", "etapa", "fecha", "centroSalud", "esActual" } ],
                "tratamientos": [ { "tipo", "frecuencia", "situacion", "fechaInicio", "esActual" } ],
                "proximasCitas": [ { "especialidad", "fechaCita", "fechaProximaCita", "dificultades" } ],
                "sintomas": [ { "severidad", "descripcion", "tieneDolor", "intensidadDolor", "ubicacionDolor" } ],
                "resumenEjecutivo": "Un resumen ejecutivo clínico redactado por la IA sobre la condición del paciente, estado de afiliación, tratamientos vigentes y alertas o necesidades identificadas."
              }
            - No inventes información. Si una tabla no tiene datos para el paciente, deja el campo correspondiente como null o array vacío.
        """.trimIndent()

        val executeSqlTool = Tool(
            functionDeclarations = listOf(
                FunctionDeclaration(
                    name = "execute_sql",
                    description = "Ejecuta una consulta SQL SELECT contra la base de datos PostgreSQL y retorna los resultados como una lista de filas.",
                    parameters = Schema(
                        type = "OBJECT",
                        properties = mapOf(
                            "query" to SchemaProperty(
                                type = "STRING",
                                description = "La consulta SQL SELECT a ejecutar. Debe ser solo de lectura y filtrar por el dni o el patient_id del paciente."
                            )
                        ),
                        required = listOf("query")
                    )
                )
            )
        )

        val chatHistory = mutableListOf<Content>()
        chatHistory.add(
            Content(
                role = "user",
                parts = listOf(Part(text = "Elabora un resumen completo en formato JSON del paciente con DNI '$dni'."))
            )
        )

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
        var maxIterations = 12

        while (maxIterations > 0) {
            maxIterations--

            val requestBody = GeminiRequest(
                contents = chatHistory,
                tools = listOf(executeSqlTool),
                systemInstruction = SystemInstruction(parts = listOf(Part(text = systemPrompt))),
                generationConfig = GenerationConfig(
                    temperature = 0.2
                )
            )

            logger.info("Calling Gemini API...")
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

            val candidate = geminiResponse?.candidates?.firstOrNull()
            val candidateContent = candidate?.content
            val candidatePart = candidateContent?.parts?.firstOrNull()

            if (candidatePart?.functionCall != null) {
                val call = candidatePart.functionCall
                if (call.name == "execute_sql") {
                    val sqlQuery = call.args?.get("query") as? String
                        ?: throw IllegalArgumentException("Missing 'query' argument in function call")

                    // Add model response to history
                    chatHistory.add(candidateContent)

                    // Execute SQL and return results
                    val sqlResult = try {
                        executeSql(sqlQuery)
                    } catch (e: Exception) {
                        logger.error("Error executing SQL generated by Gemini: ", e)
                        mapOf("error" to (e.message ?: "Unknown SQL error"))
                    }

                    // Add function response to history
                    chatHistory.add(
                        Content(
                            role = "user",
                            parts = listOf(
                                Part(
                                    functionResponse = FunctionResponse(
                                        name = "execute_sql",
                                        response = mapOf("result" to sqlResult)
                                    )
                                )
                            )
                        )
                    )
                } else {
                    throw UnsupportedOperationException("Unsupported function call: ${call.name}")
                }
            } else if (candidatePart?.text != null) {
                // We got the final text response!
                return cleanJson(candidatePart.text)
            } else {
                throw RuntimeException("La respuesta de Gemini no contiene texto ni llamadas de funciones. FinishReason: ${candidate?.finishReason}")
            }
        }

        throw RuntimeException("Se superó el número máximo de iteraciones sin recibir la respuesta final de la IA.")
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
