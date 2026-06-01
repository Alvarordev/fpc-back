package com.hazardev.fpc_back.patient.application

import com.fasterxml.jackson.databind.ObjectMapper
import com.hazardev.fpc_back.patient.domain.Enrollment
import com.hazardev.fpc_back.patient.domain.Patient
import com.hazardev.fpc_back.patient.domain.PatientDetails
import com.hazardev.fpc_back.patient.domain.PatientDiagnosis
import com.hazardev.fpc_back.patient.domain.PatientInsurance
import com.hazardev.fpc_back.patient.domain.PatientMedicalAppointment
import com.hazardev.fpc_back.patient.domain.PatientSisAffiliation
import com.hazardev.fpc_back.patient.domain.PatientSymptomReport
import com.hazardev.fpc_back.patient.domain.PatientTreatment
import com.hazardev.fpc_back.patient.infrastructure.EnrollmentRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientDetailsRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientDiagnosisRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientInsuranceRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientMedicalAppointmentRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSisAffiliationRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientSymptomReportRepository
import com.hazardev.fpc_back.patient.infrastructure.PatientTreatmentRepository
import com.hazardev.fpc_back.patient.infrastructure.gemini.GeminiClient
import jakarta.persistence.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PatientSummaryService(
    private val geminiClient: GeminiClient,
    private val patientRepository: PatientRepository,
    private val patientDetailsRepository: PatientDetailsRepository,
    private val patientInsuranceRepository: PatientInsuranceRepository,
    private val patientDiagnosisRepository: PatientDiagnosisRepository,
    private val patientTreatmentRepository: PatientTreatmentRepository,
    private val patientMedicalAppointmentRepository: PatientMedicalAppointmentRepository,
    private val patientSisAffiliationRepository: PatientSisAffiliationRepository,
    private val enrollmentRepository: EnrollmentRepository,
    private val patientSymptomReportRepository: PatientSymptomReportRepository,
    private val objectMapper: ObjectMapper
) {
    fun generateSummaryByDni(dni: String): String {
        val patient = patientRepository.findByDni(dni)
            ?: throw EntityNotFoundException("No se encontró un paciente con DNI: $dni")
        val patientId = patient.id!!

        val details = patientDetailsRepository.findByPatientId(patientId)
        val insuranceRecords = patientInsuranceRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val diagnoses = patientDiagnosisRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val treatments = patientTreatmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val appointments = patientMedicalAppointmentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val sisAffiliations = patientSisAffiliationRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
        val enrollments = enrollmentRepository.findByPatientId(patientId)
        val symptomReports = patientSymptomReportRepository.findByPatientIdOrderByCreatedAtDesc(patientId)

        val data = buildPatientData(
            patient, details, insuranceRecords, diagnoses,
            treatments, appointments, sisAffiliations, enrollments, symptomReports
        )
        val json = objectMapper.writeValueAsString(data)

        return geminiClient.generatePatientSummary(json)
    }

    private fun buildPatientData(
        patient: Patient,
        details: PatientDetails?,
        insuranceRecords: List<PatientInsurance>,
        diagnoses: List<PatientDiagnosis>,
        treatments: List<PatientTreatment>,
        appointments: List<PatientMedicalAppointment>,
        sisAffiliations: List<PatientSisAffiliation>,
        enrollments: List<Enrollment>,
        symptomReports: List<PatientSymptomReport>
    ): Map<String, Any?> {
        return mapOf(
            "paciente" to mapOf(
                "nombreCompleto" to patient.fullName,
                "dni" to patient.dni,
                "fechaNacimiento" to patient.birthDate?.toString(),
                "genero" to patient.gender,
                "telefonoPrincipal" to patient.primaryPhone,
                "telefonoSecundario" to patient.secondaryPhone,
                "tieneWhatsapp" to patient.hasWhatsapp,
                "rol" to patient.role.name,
                "estado" to patient.status.name
            ),
            "detallesPaciente" to details?.let {
                mapOf(
                    "departamentoNacimiento" to it.birthDepartment,
                    "direccionActual" to it.currentAddress,
                    "distritoActual" to it.currentDistrict,
                    "departamentoActual" to it.currentDepartment,
                    "nombreContactoEmergencia" to it.emergencyContactName,
                    "telefonoContactoEmergencia" to it.emergencyContactPhone,
                    "nivelEducativo" to it.educationLevel?.name,
                    "lenguaNativa" to it.nativeLanguage,
                    "requiereTraduccion" to it.requiresTranslation,
                    "tipoZona" to it.zoneType,
                    "tiempoViajeHospital" to it.travelTimeToHospital
                )
            },
            "seguros" to insuranceRecords.map {
                mapOf(
                    "tipo" to it.insuranceType.name,
                    "proveedorEps" to it.epsProvider?.name,
                    "esVigente" to it.isCurrent,
                    "fechaInicio" to it.startDate?.toString(),
                    "fechaFin" to it.endDate?.toString()
                )
            },
            "diagnosticos" to diagnoses.map {
                mapOf(
                    "diagnostico" to it.diagnosis,
                    "etapaCancer" to it.cancerStage?.name,
                    "fechaDiagnostico" to it.diagnosisDate?.toString(),
                    "especialidadDiagnostico" to it.diagnosisSpecialty,
                    "centroSalud" to it.healthCenter?.name,
                    "esActual" to it.isCurrent,
                    "tieneInformeMedico" to it.hasMedicalReport
                )
            },
            "tratamientos" to treatments.map {
                mapOf(
                    "diagnosticoRelacionado" to it.diagnosis.diagnosis,
                    "tipoTratamiento" to it.treatmentType,
                    "frecuenciaTratamiento" to it.treatmentFrequency,
                    "fechaInicio" to it.startDate?.toString(),
                    "fechaFin" to it.endDate?.toString(),
                    "esActual" to it.isCurrent,
                    "situacionTratamiento" to it.treatmentSituation,
                    "razonNoRecibeTratamiento" to it.notReceivingReason,
                    "centroSalud" to it.healthCenter?.name
                )
            },
            "citasMedicas" to appointments.map {
                mapOf(
                    "especialidad" to it.specialty,
                    "fechaCita" to it.appointmentDate?.toString(),
                    "fechaProximaCita" to it.nextAppointmentDate?.toString(),
                    "dificultades" to it.difficulties,
                    "esPrimeraConsulta" to it.isFirstConsultation,
                    "centroSalud" to it.healthCenter?.name
                )
            },
            "afiliacionesSIS" to sisAffiliations.map {
                mapOf(
                    "puedeAfiliarse" to it.canAffiliate,
                    "fechaAfiliacion" to it.affiliatedAt?.toString(),
                    "comentarios" to it.comments,
                    "razonNoPuedeAfiliarse" to it.cantAffiliateReason
                )
            },
            "inscripciones" to enrollments.map {
                mapOf(
                    "asistiendoConsultas" to it.currentlyAttendingConsultations,
                    "recibiendoTratamiento" to it.currentlyReceivingTreatment,
                    "fuenteIngresoPrograma" to it.entrySource,
                    "subFuenteIngreso" to it.entrySubSource,
                    "requiereTransporte" to it.requiresTransportation,
                    "tieneProblemasMovilidad" to it.hasMobilityIssues,
                    "esPacienteOncologico" to it.isOncologicalPatient
                )
            },
            "reportesSintomas" to symptomReports.map {
                mapOf(
                    "severidadMalestar" to it.discomfortSeverity,
                    "descripcionMalestar" to it.discomfortDescription,
                    "duracionSintomas" to it.symptomDuration,
                    "frecuenciaSintomas" to it.symptomFrequency,
                    "tieneDolor" to it.isPainPresent,
                    "intensidadDolor" to it.painIntensity,
                    "ubicacionDolor" to it.painLocation,
                    "descripcionDolor" to it.painDescription
                )
            }
        )
    }
}
