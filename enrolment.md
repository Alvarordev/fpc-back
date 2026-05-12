# Documentación de Flujo de Matriculación (Enrollment)

> **Audiencia**: Equipo de Frontend  
> **Idioma**: Español  
> **Versión**: Basado en el código backend actual  

---

## 1. Descripción General

La **matriculación** (*enrollment*) es el proceso mediante el cual un paciente pasa del estado `PROSPECT` (prospecto) al estado `ENROLLED` (matriculado). Existen dos modalidades:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `/api/patients/enroll` | `POST` | **Matriculación completa**: crea un paciente nuevo O matricula uno existente, con todos los datos del wizard (seguro, diagnóstico, tratamiento, citas, SIS, acompañantes, metadata). |
| `/api/patients/{id}/enroll` | `POST` | **Matriculación simple**: solo crea/actualiza el registro `PatientDetails` de un paciente existente y cambia su estado a `ENROLLED`. No procesa seguro, diagnóstico ni otros datos relacionados. |

**Reglas de negocio clave:**
- El paciente debe estar en estado `PROSPECT` para poder matricularse.
- Un paciente solo puede tener **un** registro `PatientDetails` (restricción unique).
- Las transiciones de estado son estrictas: `PROSPECT → ENROLLED → ACTIVE → INACTIVE`.
- El DNI es único (validado en creación y actualización).

---

## 2. Endpoints

---

### 2.1 `POST /api/patients/enroll` — Matriculación Completa

**Descripción**: Crea un paciente nuevo (si no se envía `patientId`) o matricula uno existente (si se envía `patientId`), procesando todos los datos del wizard de matriculación en una sola transacción atómica. Al finalizar, el paciente queda en estado `ENROLLED`.

**Response HTTP**: `201 CREATED`  
**Response Body**: [`PatientResponse`](#patientresponse) con todas las asociaciones cargadas.

#### 2.1.1 Request Body: `FullEnrollmentRequest`

A continuación se documentan **TODOS** los campos del JSON de request, agrupados por paso del wizard.

---

##### 🔹 Paso 0: Identificación del Paciente

| Campo JSON (camelCase) | Tipo | Requerido | Default | Descripción |
|-------------------------|------|-----------|---------|-------------|
| `patientId` | `string` (UUID) | No | `null` | Si se envía, matricula un paciente existente. Si es `null`, se crea uno nuevo usando `patientData`. |
| `patientData` | `object` | Sí (si `patientId` es null) | — | Datos para crear el paciente. Ignorado si se envía `patientId`. |
| `contactId` | `string` (UUID) | No | `null` | ID del contacto que inició la matriculación. **Nota**: actualmente no utilizado directamente por el backend; el contacto de enrollment se maneja vía `enrollmentMetadata`. |

##### `patientData` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `fullName` | `string` | **Sí** | — | Nombre completo del paciente. |
| `dni` | `string` | No | `null` | Número de DNI. Debe ser único en el sistema. |
| `birthDate` | `string` (ISO date `YYYY-MM-DD`) | No | `null` | Fecha de nacimiento. |
| `primaryPhone` | `string` | **Sí** | — | Teléfono principal (ej: `"987654321"`). |
| `secondaryPhone` | `string` | No | `null` | Teléfono secundario. |
| `hasWhatsapp` | `boolean` | No | `false` | ¿El número principal tiene WhatsApp? |
| `gender` | `string` | No | `null` | Género (texto libre, ej: `"Masculino"`, `"Femenino"`). |
| `role` | `string` (enum) | No | `"UNKNOWN"` | Rol: `"UNKNOWN"`, `"PATIENT"`, `"COMPANION"`. |
| `status` | `string` (enum) | No | `"PROSPECT"` | Estado inicial: `"PROSPECT"`, `"ENROLLED"`, `"ACTIVE"`, `"INACTIVE"`. |

---

##### 🔹 Paso 1: Datos Demográficos

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `details` | `object` | **Sí** | — | Datos demográficos y de contacto del paciente. Requerido para la matriculación. |

##### `details` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `birthDepartment` | `string` | No | `null` | Departamento de nacimiento (ej: `"Lima"`, `"Cusco"`). |
| `currentAddress` | `string` | No | `null` | Dirección actual de residencia. |
| `currentDistrict` | `string` | No | `null` | Distrito de residencia actual. |
| `currentDepartment` | `string` | No | `null` | Departamento de residencia actual. |
| `dniMatchesAddress` | `boolean` | No | `null` | ¿La dirección del DNI coincide con la dirección actual? |
| `travelTimeToHospital` | `string` | No | `null` | Tiempo de viaje al hospital (texto libre). |
| `emergencyContactName` | `string` | No | `null` | Nombre del contacto de emergencia. |
| `emergencyContactPhone` | `string` | No | `null` | Teléfono del contacto de emergencia. |
| `zoneType` | `string` | No | `null` | Tipo de zona (ej: `"Urbana"`, `"Rural"`). |
| `emergencyContactGender` | `string` | No | `null` | Género del contacto de emergencia. |
| `educationLevel` | `string` (enum) | No | `null` | Nivel educativo. Valores: `"INITIAL"`, `"PRIMARY_INCOMPLETE"`, `"PRIMARY"`, `"SECONDARY_INCOMPLETE"`, `"SECONDARY"`, `"TECHNICAL"`, `"TECHNICAL_INCOMPLETE"`, `"HIGHER"`, `"HIGHER_INCOMPLETE"`, `"NONE"`. |
| `nativeLanguage` | `string` | No | `null` | Lengua materna (ej: `"Español"`, `"Quechua"`). |
| `requiresTranslation` | `boolean` | No | `false` | ¿Requiere traductor/intérprete? |

---

##### 🔹 Paso 2: Seguro

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `insurance` | `object` | No | `null` | Datos del seguro del paciente. Si es `null`, no se registra seguro. |

##### `insurance` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `insuranceType` | `string` (enum) | **Sí** | — | Tipo de seguro: `"SIS"`, `"ESSALUD"`, `"EPS"`, `"FUERZAS_ARMADAS"`, `"SALUDPOL"`, `"NONE"`. |
| `epsProvider` | `string` (enum) | No | `null` | Proveedor EPS (solo si `insuranceType = "EPS"`): `"PACIFICO"`, `"RIMAC"`, `"MAPFRE"`, `"LA_POSITIVA"`, `"SANITAS"`, `"ONCOSALUD"`, `"OTHER"`. |
| `isCurrent` | `boolean` | **Sí** | — | ¿Es el seguro actual? Si es `true`, el backend marca todos los seguros anteriores como `isCurrent = false`. |
| `changeReason` | `string` | No | `null` | Motivo del cambio de seguro. |
| `startDate` | `string` (ISO date) | No | `null` | Fecha de inicio del seguro. |
| `endDate` | `string` (ISO date) | No | `null` | Fecha de fin del seguro. |
| `contactId` | `string` (UUID) | **Sí** | — | ID del contacto asociado a este registro de seguro. |

---

##### 🔹 Paso 3: Signos y Síntomas

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `symptomReport` | `object` | No | `null` | Reporte de síntomas del wizard. Si es `null`, no se registra. |

##### `symptomReport` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `hasDiscomfort` | `boolean` | No | `false` | ¿Presenta malestar o dolor? |
| `signsAndSymptoms` | `string` | No | `null` | Descripción de signos y síntomas. |
| `hasSoughtMedicalConsultation` | `boolean` | No | `false` | ¿Ya buscó consulta médica? |
| `healthCenterId` | `string` (UUID) | No | `null` | ID del centro de salud donde consultó. |
| `specialty` | `string` | No | `null` | Especialidad consultada. |
| `firstConsultationDetails` | `string` | No | `null` | Detalles de la primera consulta. |
| `indicationsReceived` | `string` | No | `null` | Indicaciones recibidas. |

---

##### 🔹 Paso 4: Diagnóstico

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `diagnosis` | `object` | No | `null` | Datos del diagnóstico. Si es `null`, no se registra. |

##### `diagnosis` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `diagnosis` | `string` | **Sí** | — | Texto del diagnóstico (ej: `"Cáncer de mama"`). |
| `cancerStage` | `string` (enum) | No | `null` | Estadío del cáncer: `"STAGE_1"`, `"STAGE_2"`, `"STAGE_3"`, `"STAGE_4"`, `"UNKNOWN"`. |
| `diagnosisDate` | `string` (ISO date) | No | `null` | Fecha del diagnóstico. |
| `healthCenterId` | `string` (UUID) | No | `null` | ID del centro de salud donde se diagnosticó. |
| `diagnosisSpecialty` | `string` | No | `null` | Especialidad que diagnosticó. |
| `symptomLeadingToCheckup` | `string` | No | `null` | Síntoma que llevó al chequeo. |
| `waitTimeForDiagnosis` | `string` | No | `null` | Tiempo de espera para el diagnóstico. |
| `hasMedicalReport` | `boolean` | No | `false` | ¿Tiene informe médico? |
| `isCurrent` | `boolean` | **Sí** | — | ¿Es el diagnóstico actual? Si `true`, marca diagnósticos anteriores como no actuales. |
| `changeReason` | `string` | No | `null` | Motivo de cambio de diagnóstico. |
| `contactId` | `string` (UUID) | **Sí** | — | ID del contacto asociado. |

---

##### 🔹 Paso 5: Tratamiento

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `treatment` | `object` | No | `null` | Datos del tratamiento. Requiere que el diagnóstico (`diagnosis`) esté registrado. |

##### `treatment` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `diagnosisId` | `string` (UUID) | **Sí** | — | ID del diagnóstico (`PatientDiagnosis`) al que se asocia este tratamiento. **Nota**: en el flujo completo, el diagnóstico se crea primero y su ID debe referenciarse aquí. ⚠️ *Ver nota al final de esta sección.* |
| `treatmentType` | `string` | **Sí** | — | Tipo de tratamiento (ej: `"Quimioterapia"`, `"Radioterapia"`). |
| `treatmentFrequency` | `string` | No | `null` | Frecuencia del tratamiento. |
| `healthCenterId` | `string` (UUID) | No | `null` | ID del centro de salud donde recibe tratamiento. |
| `startDate` | `string` (ISO date) | No | `null` | Fecha de inicio del tratamiento. |
| `endDate` | `string` (ISO date) | No | `null` | Fecha de fin del tratamiento. |
| `isCurrent` | `boolean` | **Sí** | — | ¿Es el tratamiento actual? Si `true`, marca anteriores como no actuales. |
| `changeReason` | `string` | No | `null` | Motivo de cambio de tratamiento. |
| `notReceivingReason` | `string` | No | `null` | Motivo por el cual no está recibiendo tratamiento. |
| `treatmentSituation` | `string` | No | `null` | Situación del tratamiento. |
| `contactId` | `string` (UUID) | **Sí** | — | ID del contacto asociado. |

> ⚠️ **Nota importante sobre `treatment`**: El campo `diagnosisId` requiere el ID de un registro `PatientDiagnosis` ya existente. En el flujo `fullEnrollment`, el diagnóstico se crea **antes** que el tratamiento dentro de la misma transacción, pero `diagnosisId` debe ser el UUID generado. **En la práctica actual del backend, el tratamiento se procesa después del diagnóstico en la misma transacción, pero el `diagnosisId` en el request debe ser un UUID válido (posiblemente creado en un paso anterior del wizard o generado por el frontend).**  
> 
> Si el frontend no tiene aún el ID del diagnóstico, debe enviar el tratamiento en una llamada separada a `POST /api/patients/{id}/treatments` después de obtener el ID del diagnóstico desde la respuesta del enrollment.

---

##### 🔹 Paso 5 (cont.): Citas Médicas

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `medicalAppointments` | `array` de objetos | No | `null` | Lista de citas médicas. |

##### `medicalAppointments[]` (elemento del array)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `healthCenterId` | `string` (UUID) | No | `null` | ID del centro de salud. |
| `specialty` | `string` | No | `null` | Especialidad de la cita. |
| `appointmentDate` | `string` (ISO date) | No | `null` | Fecha de la cita. |
| `nextAppointmentDate` | `string` (ISO date) | No | `null` | Fecha de la próxima cita. |
| `hasReferralSheet` | `boolean` | No | `false` | ¿Tiene hoja de referencia? |
| `referredTo` | `string` | No | `null` | Referido a qué especialidad/centro. |
| `difficulties` | `string` | No | `null` | Dificultades reportadas. |
| `isFirstConsultation` | `boolean` | No | `false` | ¿Es primera consulta? |
| `contactId` | `string` (UUID) | **Sí** | — | ID del contacto asociado. |

---

##### 🔹 Paso 5 (cont.): Afiliación SIS

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `sisAffiliation` | `object` | No | `null` | Datos de afiliación al SIS. **Solo se procesa si el paciente NO tiene un seguro real** (es decir, `insurance` es `null` o `insurance.insuranceType = "NONE"`). |

##### `sisAffiliation` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `canAffiliate` | `boolean` | **Sí** | — | ¿El paciente puede afiliarse al SIS? |
| `expectedDate` | `string` (ISO date) | No | `null` | Fecha esperada de afiliación. |
| `cantAffiliateReason` | `string` | No | `null` | Motivo por el cual no puede afiliarse. |
| `comments` | `string` | No | `null` | Comentarios adicionales. |
| `contactId` | `string` (UUID) | **Sí** | — | ID del contacto asociado. |

---

##### 🔹 Paso 5 (cont.): Acompañantes

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `companions` | `array` de objetos | No | `null` | Lista de acompañantes a vincular. El acompañante debe existir como Patient con `role = "COMPANION"`. |

##### `companions[]` (elemento del array)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `companionId` | `string` (UUID) | **Sí** | — | ID del paciente que actúa como acompañante. |
| `isPrimaryInformant` | `boolean` | No | `false` | ¿Es el informante principal? |

---

##### 🔹 Paso 5 (cont.): Metadata de la Matriculación

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `enrollmentMetadata` | `object` | No | `null` | Metadata del wizard de matriculación. Controla la creación/transición del Contacto de enrollment y la creación del registro `Enrollment`. |

##### `enrollmentMetadata` (sub-objeto)

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `caseComments` | `string` | No | `null` | Comentarios del caso. Se guardan como `notes` en el Contact. |
| `startTime` | `string` (ISO instant) | No | `null` | Hora de inicio del contacto (UTC). Se mapea a `scheduledAt` del Contact. |
| `endTime` | `string` (ISO instant) | No | `null` | Hora de fin del contacto (UTC). Se mapea a `completedAt` del Contact. Si es `null`, se usa la hora actual. |
| `dataPolicyAccepted` | `boolean` | No | `false` | ¿Aceptó la política de datos? → `consentToContact`. |
| `informedConsentAccepted` | `boolean` | No | `false` | ¿Aceptó el consentimiento informado? → `consentToShareData`. |
| `affiliationType` | `string` (enum) | No | `"PATIENT"` | Tipo de afiliación: `"PATIENT"` (paciente), `"FAMILY"` (familiar). |
| `isOncologicalPatient` | `boolean` | No | `false` | ¿Es paciente oncológico? |
| `programEntryPoint` | `string` | No | `null` | Punto de entrada al programa → `entrySource`. |
| `currentlyAttendingConsultations` | `boolean` | No | `null` | ¿Actualmente asiste a consultas? |
| `currentlyReceivingTreatment` | `boolean` | No | `null` | ¿Actualmente recibe tratamiento? |
| `surveyAccepted` | `boolean` | No | `false` | ¿Aceptó la encuesta? |
| `agentId` | `string` (UUID) | No | `null` | ID del agente que realiza la matriculación. Se asigna al Contact. |

---

#### 2.1.2 Response: `PatientResponse`

```json
{
  "id": "uuid",
  "fullName": "string",
  "dni": "string | null",
  "birthDate": "YYYY-MM-DD | null",
  "primaryPhone": "string",
  "secondaryPhone": "string | null",
  "hasWhatsapp": "boolean",
  "gender": "string | null",
  "role": "PATIENT | COMPANION | UNKNOWN",
  "status": "ENROLLED",
  "createdAt": "ISO datetime",
  "updatedAt": "ISO datetime",
  "details": { ... PatientDetailsResponse },
  "insurance": [ ... InsuranceRecordResponse[] ],
  "diagnoses": [ ... DiagnosisRecordResponse[] ],
  "treatments": [ ... TreatmentRecordResponse[] ],
  "medicalAppointments": [ ... MedicalAppointmentResponse[] ],
  "sisAffiliations": [ ... SisAffiliationResponse[] ],
  "companions": [ ... CompanionResponse[] ],
  "contacts": [ ... ContactResponse[] ],
  "enrollments": [ ... EnrollmentMetadataResponse[] ],
  "symptomReports": [ ... SymptomReportResponse[] ]
}
```

---

### 2.2 `POST /api/patients/{id}/enroll` — Matriculación Simple

**Descripción**: Crea (o actualiza) el registro `PatientDetails` para un paciente existente y cambia su estado a `ENROLLED`. Este endpoint **no** procesa seguro, diagnóstico, tratamiento, citas, SIS, acompañantes ni metadata. Es útil cuando solo se necesita completar los datos demográficos del paciente.

**Path Parameter**:
| Parámetro | Tipo | Descripción |
|-----------|------|-------------|
| `id` | `string` (UUID) | ID del paciente a matricular. |

**Response HTTP**: `201 CREATED`  

#### 2.2.1 Request Body: `EnrollPatientRequest`

| Campo JSON | Tipo | Requerido | Default | Descripción |
|------------|------|-----------|---------|-------------|
| `birthDepartment` | `string` | No | `null` | Departamento de nacimiento. |
| `currentAddress` | `string` | No | `null` | Dirección actual. |
| `currentDistrict` | `string` | No | `null` | Distrito actual. |
| `currentDepartment` | `string` | No | `null` | Departamento actual. |
| `dniMatchesAddress` | `boolean` | No | `null` | ¿DNI coincide con dirección? |
| `travelTimeToHospital` | `string` | No | `null` | Tiempo de viaje al hospital. |
| `emergencyContactName` | `string` | No | `null` | Nombre contacto emergencia. |
| `emergencyContactPhone` | `string` | No | `null` | Teléfono contacto emergencia. |
| `zoneType` | `string` | No | `null` | Tipo de zona. |
| `emergencyContactGender` | `string` | No | `null` | Género contacto emergencia. |
| `educationLevel` | `string` (enum) | No | `null` | Nivel educativo (ver valores en [Paso 1](#-paso-1-datos-demográficos)). |
| `nativeLanguage` | `string` | No | `null` | Lengua materna. |
| `requiresTranslation` | `boolean` | No | `false` | ¿Requiere traducción? |

---

## 3. Flujo del Contacto (CRÍTICO para el Frontend)

El Contacto de enrollment se gestiona **exclusivamente** a través del campo `enrollmentMetadata` en el endpoint de matriculación completa. Hay dos escenarios:

---

### Escenario A: Paciente NUEVO (`patientId = null`)

1. El backend crea un nuevo `Patient` con los datos de `patientData`. El estado inicial es `PROSPECT`.
2. El backend resuelve el contacto de enrollment llamando a `resolveEnrollmentContact()`:
   - Como el paciente es nuevo, **no** tiene contactos previos con `purpose = ENROLLMENT` y `status = SCHEDULED`.
   - Se **crea un nuevo Contact** con los siguientes valores:
     - `patient` = el paciente recién creado
     - `agent` = el agente con ID `enrollmentMetadata.agentId` (si se envió)
     - `type` = `WHATSAPP` (fijo)
     - `status` = `COMPLETED`
     - `purpose` = `ENROLLMENT`
     - `scheduledAt` = `enrollmentMetadata.startTime` (convertido de Instant a LocalDateTime en UTC)
     - `completedAt` = `enrollmentMetadata.endTime` (o `LocalDateTime.now()` si es null)
     - `notes` = `enrollmentMetadata.caseComments`
3. Se crea el registro `Enrollment` vinculado al `Patient` y al `Contact` creado, con los campos de metadata (`consentToContact`, `consentToShareData`, `isOncologicalPatient`, `entrySource`, etc.).
4. Opcionalmente, si se envió `symptomReport`, se crea `PatientSymptomReport` vinculado al `Patient`, al `Contact` y al `Enrollment`.
5. Se procesan el resto de sub-entidades (details, insurance, diagnosis, treatment, etc.).
6. El estado del paciente se cambia a `ENROLLED`.

```
┌─────────────────────────────────────────────────────────────────────────┐
│ Escenario A — Paciente NUEVO                                            │
│                                                                         │
│  1. Crear Patient (status=PROSPECT)                                     │
│                   │                                                     │
│  2. Crear Contact (purpose=ENROLLMENT, status=COMPLETED, type=WHATSAPP) │
│       con agentId, startTime, endTime, caseComments                     │
│                   │                                                     │
│  3. Crear Enrollment (vinculado a Patient + Contact)                    │
│                   │                                                     │
│  4. Opcional: Crear PatientSymptomReport                                │
│                   │                                                     │
│  5. Crear/Actualizar PatientDetails                                     │
│                   │                                                     │
│  6. Procesar Insurance, Diagnosis, Treatment, Appointments,              │
│     SIS, Companions                                                     │
│                   │                                                     │
│  7. Patient.status = ENROLLED                                           │
└─────────────────────────────────────────────────────────────────────────┘
```

---

### Escenario B: Paciente EXISTENTE (`patientId` proporcionado)

1. El backend busca el `Patient` por ID. **Debe estar en estado `PROSPECT`**.
2. Si `patientData` fue enviado, se actualizan los campos del paciente (`fullName`, `dni`, `birthDate`, `primaryPhone`, etc.).
3. El backend busca entre todos los `Contact` del paciente:
   - Filtra por `purpose = ENROLLMENT` **y** `status = SCHEDULED`.
4. **Si existe un contacto SCHEDULED**:
   - Transiciona su estado de `SCHEDULED` a `COMPLETED`.
   - Actualiza `completedAt` = `enrollmentMetadata.endTime` (o `now()` si es null).
   - Si se envió `agentId`, actualiza el agente.
   - Si se envió `caseComments`, actualiza `notes`.
   - Si se envió `startTime`, actualiza `scheduledAt`.
5. **Si NO existe un contacto SCHEDULED**:
   - Crea un nuevo `Contact` con los mismos valores que en el Escenario A (`status = COMPLETED`, `purpose = ENROLLMENT`, `type = WHATSAPP`).
6. Se crea el registro `Enrollment` vinculado al `Patient` y al `Contact` resuelto.
7. Opcionalmente se crea `PatientSymptomReport`.
8. Se procesan el resto de sub-entidades.
9. El estado del paciente se cambia a `ENROLLED`.

```
┌──────────────────────────────────────────────────────────────────────┐
│ Escenario B — Paciente EXISTENTE                                     │
│                                                                      │
│  1. Buscar Patient por ID (debe ser PROSPECT)                        │
│                   │                                                  │
│  2. Opcional: Actualizar datos del paciente (patientData)            │
│                   │                                                  │
│  3. Buscar Contact con purpose=ENROLLMENT + status=SCHEDULED         │
│                   │                                                  │
│     ┌── ¿Existe? ──SÍ──► Transicionar SCHEDULED → COMPLETED         │
│     │                         Actualizar datos del contacto          │
│     │                                                                 │
│     └── NO ───► Crear nuevo Contact COMPLETED (igual que Esc. A)    │
│                   │                                                  │
│  4. Crear Enrollment (vinculado a Patient + Contact)                 │
│                   │                                                  │
│  5. Opcional: Crear PatientSymptomReport                             │
│                   │                                                  │
│  6. Procesar Insurance, Diagnosis, Treatment, Appointments,          │
│     SIS, Companions                                                  │
│                   │                                                  │
│  7. Patient.status = ENROLLED                                        │
└──────────────────────────────────────────────────────────────────────┘
```

---

### Resumen: ¿Cuándo se crea un Contact nuevo vs se reutiliza?

| Condición | Acción |
|-----------|--------|
| Paciente NUEVO | Siempre se crea un Contact nuevo con `status = COMPLETED` |
| Paciente EXISTENTE, existe Contact con `purpose=ENROLLMENT` y `status=SCHEDULED` | Se transiciona a `COMPLETED` y se actualizan sus campos |
| Paciente EXISTENTE, NO existe Contact SCHEDULED de enrollment | Se crea un Contact nuevo con `status = COMPLETED` |

---

## 4. Tablas y Relaciones

### 4.1 Diagrama de Relaciones

```
┌──────────────────────┐
│        Agent         │
│  ┌────────────────┐  │
│  │ id (UUID, PK)  │  │
│  │ fullName       │  │
│  │ ...            │  │
│  └───────┬────────┘  │
└──────────│───────────┘
           │ 1
           │
           │ N
┌──────────▼───────────────────────────────────────────────┐
│                       Contact                             │
│  ┌────────────────────────────────────────────────────┐  │
│  │ id (UUID, PK)                                     │  │
│  │ patient_id (FK → patients.id, NOT NULL)           │  │
│  │ agent_id (FK → agents.id, NULLABLE)               │  │
│  │ type (ContactType: WHATSAPP|CALL|VIDEO_CALL|...)  │  │
│  │ status (ContactStatus: SCHEDULED|COMPLETED|...)   │  │
│  │ purpose (ContactPurpose: FIRST_CONTACT|            │  │
│  │          ENROLLMENT|FOLLOW_UP|...)                 │  │
│  │ scheduled_at (LocalDateTime, NULLABLE)            │  │
│  │ completed_at (LocalDateTime, NULLABLE)            │  │
│  │ notes (String, NULLABLE)                          │  │
│  │ scheduled_next_contact_id (FK → contacts.id)      │  │
│  │ created_at, updated_at                            │  │
│  └───────────────────────┬────────────────────────────┘  │
└──────────────────────────│───────────────────────────────┘
                           │ N
                           │
┌──────────────────────────▼─────────────────────────────────────────────────────┐
│                                   Patient                                        │
│  ┌──────────────────────────────────────────────────────────────────────────┐  │
│  │ id (UUID, PK)                                                            │  │
│  │ full_name (String, NOT NULL)                                             │  │
│  │ dni (String, NULLABLE, UNIQUE)                                           │  │
│  │ birth_date (LocalDate, NULLABLE)                                         │  │
│  │ primary_phone (String, NOT NULL)                                         │  │
│  │ secondary_phone (String, NULLABLE)                                       │  │
│  │ gender (String, NULLABLE)                                                │  │
│  │ has_whatsapp (Boolean, NOT NULL, DEFAULT false)                          │  │
│  │ role (PatientRole: UNKNOWN|PATIENT|COMPANION, NOT NULL)                  │  │
│  │ status (PatientStatus: PROSPECT|ENROLLED|ACTIVE|INACTIVE, NOT NULL)      │  │
│  │ created_at, updated_at                                                   │  │
│  └──────────────────────────────────────────────────────────────────────────┘  │
└───────┬───────┬──────────┬──────────┬──────────┬──────────┬──────────┬──────────┘
        │ 1     │ 1        │ 1        │ 1        │ 1        │ 1        │ 1
        │       │           │           │           │           │           │
   ┌────▼───┐ ┌─▼────────┐ ┌▼────────┐ ┌▼────────┐ ┌▼────────┐ ┌▼──────────┐ ┌▼───────────────────┐
   │Patient │ │Enrollment│ │Patient  │ │Patient  │ │Patient  │ │Patient    │ │PatientSisAffiliation│
   │Details │ │          │ │Insurance│ │Diagnosis│ │Treatment│ │SymptomRpt│ │                     │
   │ 1:1    │ │ 1:N      │ │ 1:N     │ │ 1:N     │ │ 1:N     │ │ 1:N      │ │ 1:N                 │
   └────────┘ └──┬───────┘ └──┬──────┘ └──┬──────┘ └──┬──────┘ └──┬───────┘ └──┬─────────────────┘
                 │            │           │           │           │            │
                 │ N          │ N         │ N         │ N         │ N          │ N
                 └────────────┴───────────┴───────────┴───────────┴────────────┘
                                      │
                                      │ (todas las sub-entidades referencian Contact)
                                      ▼
                                  Contact

┌──────────────────────────────────────┐
│         CompanionPatient              │
│  ┌────────────────────────────────┐  │
│  │ id (UUID, PK)                 │  │
│  │ companion_id (FK → patients)  │  │
│  │ patient_id (FK → patients)    │  │
│  │ is_primary_informant (Bool)   │  │
│  │ created_at                    │  │
│  └────────────────────────────────┘  │
│  (Tabla puente: Patient ←→ Patient)  │
└──────────────────────────────────────┘

┌──────────────────────────────────────────────┐
│           PatientMedicalAppointment           │
│  ┌────────────────────────────────────────┐  │
│  │ id (UUID, PK)                         │  │
│  │ patient_id (FK → patients)            │  │
│  │ contact_id (FK → contacts)            │  │
│  │ health_center_id (FK, NULLABLE)       │  │
│  │ specialty, appointment_date, ...      │  │
│  └────────────────────────────────────────┘  │
└──────────────────────────────────────────────┘
```

### 4.2 Columnas de Cada Tabla

#### `patients`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto (UUID v4) | Identificador único. |
| `full_name` | VARCHAR | NOT NULL | — | Nombre completo. |
| `dni` | VARCHAR | NULLABLE, UNIQUE | `null` | Número de DNI, único. |
| `birth_date` | DATE | NULLABLE | `null` | Fecha de nacimiento. |
| `primary_phone` | VARCHAR | NOT NULL | — | Teléfono principal. |
| `secondary_phone` | VARCHAR | NULLABLE | `null` | Teléfono secundario. |
| `gender` | VARCHAR | NULLABLE | `null` | Género (texto libre). |
| `has_whatsapp` | BOOLEAN | NOT NULL | `false` | ¿WhatsApp? |
| `role` | VARCHAR (enum) | NOT NULL | `UNKNOWN` | Rol: `UNKNOWN`, `PATIENT`, `COMPANION`. |
| `status` | VARCHAR (enum) | NOT NULL | `PROSPECT` | Estado: `PROSPECT`, `ENROLLED`, `ACTIVE`, `INACTIVE`. |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |
| `updated_at` | TIMESTAMP | NOT NULL | auto | Fecha de última actualización. |

#### `patient_details` (1:1 con Patient)

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL, UNIQUE | — | FK a `patients.id`. |
| `birth_department` | VARCHAR | NULLABLE | `null` | Departamento de nacimiento. |
| `current_address` | VARCHAR | NULLABLE | `null` | Dirección actual. |
| `current_district` | VARCHAR | NULLABLE | `null` | Distrito actual. |
| `current_department` | VARCHAR | NULLABLE | `null` | Departamento actual. |
| `dni_matches_address` | BOOLEAN | NULLABLE | `null` | ¿DNI coincide con dirección? |
| `travel_time_to_hospital` | VARCHAR | NULLABLE | `null` | Tiempo de viaje al hospital. |
| `emergency_contact_name` | VARCHAR | NULLABLE | `null` | Nombre contacto emergencia. |
| `emergency_contact_phone` | VARCHAR | NULLABLE | `null` | Teléfono contacto emergencia. |
| `zone_type` | VARCHAR | NULLABLE | `null` | Tipo de zona. |
| `emergency_contact_gender` | VARCHAR | NULLABLE | `null` | Género contacto emergencia. |
| `education_level` | VARCHAR (enum) | NULLABLE | `null` | Nivel educativo. |
| `native_language` | VARCHAR | NULLABLE | `null` | Lengua materna. |
| `requires_translation` | BOOLEAN | NOT NULL | `false` | ¿Requiere traducción? |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |
| `updated_at` | TIMESTAMP | NOT NULL | auto | Fecha de actualización. |

#### `contacts`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `agent_id` | UUID (FK) | NULLABLE | `null` | FK a `agents.id`. |
| `type` | VARCHAR (enum) | NOT NULL | — | Tipo: `WHATSAPP`, `CALL`, `VIDEO_CALL`, `EMAIL`, `IN_PERSON`. |
| `status` | VARCHAR (enum) | NOT NULL | — | Estado: `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_ANSWER`. |
| `purpose` | VARCHAR (enum) | NOT NULL | — | Propósito: `FIRST_CONTACT`, `ENROLLMENT`, `FOLLOW_UP`, `PSYCHOONCOLOGY_REFERRAL`, `OTHER`. |
| `scheduled_at` | TIMESTAMP | NULLABLE | `null` | Fecha/hora programada. |
| `completed_at` | TIMESTAMP | NULLABLE | `null` | Fecha/hora de finalización. |
| `notes` | VARCHAR | NULLABLE | `null` | Notas del contacto. |
| `scheduled_next_contact_id` | UUID (FK) | NULLABLE | `null` | FK recursiva a `contacts.id`. |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |
| `updated_at` | TIMESTAMP | NOT NULL | auto | Fecha de actualización. |

#### `enrollments`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `contact_id` | UUID (FK) | NOT NULL | — | FK a `contacts.id` (el contacto de enrollment). |
| `currently_attending_consultations` | BOOLEAN | NULLABLE | `null` | ¿Asiste a consultas actualmente? |
| `currently_receiving_treatment` | BOOLEAN | NULLABLE | `null` | ¿Recibe tratamiento actualmente? |
| `entry_source` | VARCHAR | NULLABLE | `null` | Punto de entrada al programa. |
| `entry_sub_source` | VARCHAR | NULLABLE | `null` | Sub-punto de entrada. |
| `consent_to_contact` | BOOLEAN | NULLABLE | `null` | Aceptó política de datos/contacto. |
| `consent_to_share_data` | BOOLEAN | NULLABLE | `null` | Aceptó consentimiento informado. |
| `affiliation_type` | VARCHAR (enum) | NULLABLE | `null` | Tipo de afiliación: `PATIENT`, `FAMILY`. |
| `affiliated_patient_name` | VARCHAR | NULLABLE | `null` | Nombre del paciente afiliado (si FAMILY). |
| `affiliated_patient_dni` | VARCHAR | NULLABLE | `null` | DNI del paciente afiliado (si FAMILY). |
| `requires_transportation` | BOOLEAN | NULLABLE | `null` | ¿Requiere transporte? |
| `has_mobility_issues` | BOOLEAN | NULLABLE | `null` | ¿Tiene problemas de movilidad? |
| `is_oncological_patient` | BOOLEAN | NOT NULL | `false` | ¿Es paciente oncológico? |
| `survey_accepted` | BOOLEAN | NOT NULL | `false` | ¿Aceptó la encuesta? |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

#### `patient_symptom_reports`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `contact_id` | UUID (FK) | NOT NULL | — | FK a `contacts.id`. |
| `enrollment_id` | UUID (FK) | NULLABLE | `null` | FK a `enrollments.id`. |
| `discomfort_severity` | VARCHAR | NULLABLE | `null` | Severidad del malestar (mapea de `indicationsReceived`). |
| `discomfort_description` | TEXT | NULLABLE | `null` | Descripción del malestar (mapea de `signsAndSymptoms`). |
| `symptom_duration` | VARCHAR | NULLABLE | `null` | Duración de síntomas. |
| `symptom_frequency` | VARCHAR | NULLABLE | `null` | Frecuencia de síntomas. |
| `is_pain_present` | BOOLEAN | NULLABLE | `null` | ¿Hay dolor? (mapea de `hasDiscomfort`). |
| `pain_intensity` | INT | NULLABLE | `null` | Intensidad del dolor (1-10). |
| `pain_location` | VARCHAR | NULLABLE | `null` | Ubicación del dolor. |
| `pain_description` | TEXT | NULLABLE | `null` | Descripción del dolor (mapea de `firstConsultationDetails`). |
| `has_sought_medical_consultation` | BOOLEAN | NOT NULL | `false` | ¿Buscó consulta médica? |
| `health_center_id` | UUID | NULLABLE | `null` | ID del centro de salud. |
| `specialty` | VARCHAR | NULLABLE | `null` | Especialidad. |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

#### `patient_insurance`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `contact_id` | UUID (FK) | NOT NULL | — | FK a `contacts.id`. |
| `insurance_type` | VARCHAR (enum) | NOT NULL | — | Tipo: `SIS`, `ESSALUD`, `EPS`, `FUERZAS_ARMADAS`, `SALUDPOL`, `NONE`. |
| `eps_provider` | VARCHAR (enum) | NULLABLE | `null` | Proveedor EPS: `PACIFICO`, `RIMAC`, `MAPFRE`, `LA_POSITIVA`, `SANITAS`, `ONCOSALUD`, `OTHER`. |
| `is_current` | BOOLEAN | NOT NULL | — | ¿Es el seguro actual? |
| `change_reason` | VARCHAR | NULLABLE | `null` | Motivo de cambio. |
| `start_date` | DATE | NULLABLE | `null` | Fecha de inicio. |
| `end_date` | DATE | NULLABLE | `null` | Fecha de fin. |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

#### `patient_diagnoses`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `contact_id` | UUID (FK) | NOT NULL | — | FK a `contacts.id`. |
| `diagnosis` | VARCHAR | NOT NULL | — | Texto del diagnóstico. |
| `cancer_stage` | VARCHAR (enum) | NULLABLE | `null` | Estadío: `STAGE_1`, `STAGE_2`, `STAGE_3`, `STAGE_4`, `UNKNOWN`. |
| `diagnosis_date` | DATE | NULLABLE | `null` | Fecha del diagnóstico. |
| `health_center_id` | UUID (FK) | NULLABLE | `null` | FK a `health_centers.id`. |
| `diagnosis_specialty` | VARCHAR | NULLABLE | `null` | Especialidad que diagnosticó. |
| `symptom_leading_to_checkup` | VARCHAR | NULLABLE | `null` | Síntoma que llevó al chequeo. |
| `wait_time_for_diagnosis` | VARCHAR | NULLABLE | `null` | Tiempo de espera. |
| `has_medical_report` | BOOLEAN | NOT NULL | `false` | ¿Tiene informe médico? |
| `is_current` | BOOLEAN | NOT NULL | — | ¿Diagnóstico actual? |
| `change_reason` | VARCHAR | NULLABLE | `null` | Motivo de cambio. |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

#### `patient_treatments`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `contact_id` | UUID (FK) | NOT NULL | — | FK a `contacts.id`. |
| `diagnosis_id` | UUID (FK) | NOT NULL | — | FK a `patient_diagnoses.id`. |
| `treatment_type` | VARCHAR | NOT NULL | — | Tipo de tratamiento. |
| `treatment_frequency` | VARCHAR | NULLABLE | `null` | Frecuencia. |
| `health_center_id` | UUID (FK) | NULLABLE | `null` | FK a `health_centers.id`. |
| `start_date` | DATE | NULLABLE | `null` | Fecha de inicio. |
| `end_date` | DATE | NULLABLE | `null` | Fecha de fin. |
| `is_current` | BOOLEAN | NOT NULL | — | ¿Tratamiento actual? |
| `change_reason` | VARCHAR | NULLABLE | `null` | Motivo de cambio. |
| `not_receiving_reason` | VARCHAR | NULLABLE | `null` | Motivo de no recibir. |
| `treatment_situation` | VARCHAR | NULLABLE | `null` | Situación del tratamiento. |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

#### `patient_medical_appointments`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `contact_id` | UUID (FK) | NOT NULL | — | FK a `contacts.id`. |
| `health_center_id` | UUID (FK) | NULLABLE | `null` | FK a `health_centers.id`. |
| `specialty` | VARCHAR | NULLABLE | `null` | Especialidad. |
| `appointment_date` | DATE | NULLABLE | `null` | Fecha de la cita. |
| `next_appointment_date` | DATE | NULLABLE | `null` | Fecha de próxima cita. |
| `has_referral_sheet` | BOOLEAN | NOT NULL | `false` | ¿Tiene hoja de referencia? |
| `referred_to` | VARCHAR | NULLABLE | `null` | Referido a. |
| `difficulties` | VARCHAR | NULLABLE | `null` | Dificultades. |
| `is_first_consultation` | BOOLEAN | NOT NULL | `false` | ¿Es primera consulta? |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

#### `patient_sis_affiliation`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id`. |
| `contact_id` | UUID (FK) | NOT NULL | — | FK a `contacts.id`. |
| `can_affiliate` | BOOLEAN | NOT NULL | — | ¿Puede afiliarse? |
| `expected_date` | DATE | NULLABLE | `null` | Fecha esperada. |
| `cant_affiliate_reason` | VARCHAR | NULLABLE | `null` | Motivo de no poder afiliarse. |
| `affiliated_at` | TIMESTAMP | NULLABLE | `null` | Fecha de afiliación efectiva. |
| `comments` | TEXT | NULLABLE | `null` | Comentarios. |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

#### `companion_patient`

| Columna | Tipo | Nullable | Default | Descripción |
|---------|------|----------|---------|-------------|
| `id` | UUID | NOT NULL | auto | PK |
| `companion_id` | UUID (FK) | NOT NULL | — | FK a `patients.id` (el acompañante). |
| `patient_id` | UUID (FK) | NOT NULL | — | FK a `patients.id` (el paciente). |
| `is_primary_informant` | BOOLEAN | NOT NULL | `false` | ¿Informante principal? |
| `created_at` | TIMESTAMP | NOT NULL | auto | Fecha de creación. |

---

### 4.3 Valores de Enums

| Enum | Valores |
|------|---------|
| `PatientStatus` | `PROSPECT`, `ENROLLED`, `ACTIVE`, `INACTIVE` |
| `PatientRole` | `UNKNOWN`, `PATIENT`, `COMPANION` |
| `ContactType` | `WHATSAPP`, `CALL`, `VIDEO_CALL`, `EMAIL`, `IN_PERSON` |
| `ContactStatus` | `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_ANSWER` |
| `ContactPurpose` | `FIRST_CONTACT`, `ENROLLMENT`, `FOLLOW_UP`, `PSYCHOONCOLOGY_REFERRAL`, `OTHER` |
| `InsuranceType` | `SIS`, `ESSALUD`, `EPS`, `FUERZAS_ARMADAS`, `SALUDPOL`, `NONE` |
| `EpsProvider` | `PACIFICO`, `RIMAC`, `MAPFRE`, `LA_POSITIVA`, `SANITAS`, `ONCOSALUD`, `OTHER` |
| `CancerStage` | `STAGE_1`, `STAGE_2`, `STAGE_3`, `STAGE_4`, `UNKNOWN` |
| `EducationLevel` | `INITIAL`, `PRIMARY_INCOMPLETE`, `PRIMARY`, `SECONDARY_INCOMPLETE`, `SECONDARY`, `TECHNICAL`, `TECHNICAL_INCOMPLETE`, `HIGHER`, `HIGHER_INCOMPLETE`, `NONE` |
| `AffiliationType` | `PATIENT`, `FAMILY` |

---

## 5. Ejemplos de Request JSON

### 5.1 Escenario A: Paciente Nuevo

```json
{
  "patientId": null,
  "patientData": {
    "fullName": "María García López",
    "dni": "12345678",
    "birthDate": "1985-03-15",
    "primaryPhone": "987654321",
    "secondaryPhone": null,
    "hasWhatsapp": true,
    "gender": "Femenino",
    "role": "PATIENT"
  },
  "details": {
    "birthDepartment": "Lima",
    "currentAddress": "Av. Los Olivos 123",
    "currentDistrict": "San Martín de Porres",
    "currentDepartment": "Lima",
    "dniMatchesAddress": false,
    "travelTimeToHospital": "45 minutos",
    "emergencyContactName": "Carlos García",
    "emergencyContactPhone": "987111222",
    "zoneType": "Urbana",
    "emergencyContactGender": "Masculino",
    "educationLevel": "SECONDARY",
    "nativeLanguage": "Español",
    "requiresTranslation": false
  },
  "insurance": {
    "insuranceType": "SIS",
    "epsProvider": null,
    "isCurrent": true,
    "changeReason": null,
    "startDate": "2024-01-01",
    "endDate": null,
    "contactId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "diagnosis": {
    "diagnosis": "Cáncer de mama",
    "cancerStage": "STAGE_2",
    "diagnosisDate": "2024-06-10",
    "healthCenterId": "660e8400-e29b-41d4-a716-446655440001",
    "diagnosisSpecialty": "Oncología",
    "symptomLeadingToCheckup": "Bulto en el seno",
    "waitTimeForDiagnosis": "3 semanas",
    "hasMedicalReport": true,
    "isCurrent": true,
    "changeReason": null,
    "contactId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "treatment": {
    "diagnosisId": "<ID_DEL_DIAGNÓSTICO_CREADO>",
    "treatmentType": "Quimioterapia",
    "treatmentFrequency": "Cada 3 semanas",
    "healthCenterId": "660e8400-e29b-41d4-a716-446655440001",
    "startDate": "2024-07-01",
    "endDate": null,
    "isCurrent": true,
    "changeReason": null,
    "notReceivingReason": null,
    "treatmentSituation": "En curso",
    "contactId": "550e8400-e29b-41d4-a716-446655440000"
  },
  "medicalAppointments": [
    {
      "healthCenterId": "660e8400-e29b-41d4-a716-446655440001",
      "specialty": "Oncología",
      "appointmentDate": "2024-07-15",
      "nextAppointmentDate": "2024-08-15",
      "hasReferralSheet": true,
      "referredTo": null,
      "difficulties": null,
      "isFirstConsultation": false,
      "contactId": "550e8400-e29b-41d4-a716-446655440000"
    }
  ],
  "sisAffiliation": null,
  "companions": null,
  "enrollmentMetadata": {
    "caseComments": "Paciente derivada del hospital de la solidaridad",
    "startTime": "2024-07-10T09:00:00Z",
    "endTime": "2024-07-10T09:45:00Z",
    "dataPolicyAccepted": true,
    "informedConsentAccepted": true,
    "affiliationType": "PATIENT",
    "isOncologicalPatient": true,
    "programEntryPoint": "Derivación hospitalaria",
    "currentlyAttendingConsultations": true,
    "currentlyReceivingTreatment": true,
    "surveyAccepted": true,
    "agentId": "770e8400-e29b-41d4-a716-446655440002"
  },
  "symptomReport": {
    "hasDiscomfort": true,
    "signsAndSymptoms": "Dolor en el seno derecho, fatiga general",
    "hasSoughtMedicalConsultation": true,
    "healthCenterId": "660e8400-e29b-41d4-a716-446655440001",
    "specialty": "Oncología",
    "firstConsultationDetails": "Consulta inicial con mastólogo en junio 2024",
    "indicationsReceived": "Iniciar quimioterapia y controles mensuales"
  },
  "contactId": null
}
```

### 5.2 Escenario B: Paciente Existente

```json
{
  "patientId": "880e8400-e29b-41d4-a716-446655440003",
  "patientData": {
    "fullName": "María García López",
    "dni": "12345678",
    "birthDate": "1985-03-15",
    "primaryPhone": "987654321",
    "secondaryPhone": "999888777",
    "hasWhatsapp": true,
    "gender": "Femenino",
    "role": "PATIENT"
  },
  "details": {
    "birthDepartment": "Lima",
    "currentAddress": "Jr. Las Flores 456",
    "currentDistrict": "Los Olivos",
    "currentDepartment": "Lima",
    "dniMatchesAddress": false,
    "travelTimeToHospital": "30 minutos",
    "emergencyContactName": "Carlos García",
    "emergencyContactPhone": "987111222",
    "zoneType": "Urbana",
    "emergencyContactGender": "Masculino",
    "educationLevel": "TECHNICAL",
    "nativeLanguage": "Español",
    "requiresTranslation": false
  },
  "insurance": {
    "insuranceType": "ESSALUD",
    "epsProvider": null,
    "isCurrent": true,
    "changeReason": null,
    "startDate": "2024-01-01",
    "endDate": null,
    "contactId": "990e8400-e29b-41d4-a716-446655440004"
  },
  "diagnosis": null,
  "treatment": null,
  "medicalAppointments": null,
  "sisAffiliation": null,
  "companions": null,
  "enrollmentMetadata": {
    "caseComments": "Paciente contactada vía WhatsApp - ya estaba registrada como PROSPECT",
    "startTime": "2024-08-01T14:00:00Z",
    "endTime": "2024-08-01T14:30:00Z",
    "dataPolicyAccepted": true,
    "informedConsentAccepted": true,
    "affiliationType": "PATIENT",
    "isOncologicalPatient": false,
    "programEntryPoint": "WhatsApp",
    "currentlyAttendingConsultations": true,
    "currentlyReceivingTreatment": false,
    "surveyAccepted": true,
    "agentId": "770e8400-e29b-41d4-a716-446655440002"
  },
  "symptomReport": null,
  "contactId": null
}
```

---

## 6. Flujo del Wizard (para el Frontend)

A continuación se mapea cada paso del wizard de matriculación al campo correspondiente del JSON de `FullEnrollmentRequest`.

```
┌──────────────────────────────────────────────────────────────────┐
│                    WIZARD DE MATRICULACIÓN                        │
│                                                                   │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ PASO 0: Identificación del paciente                         │ │
│  │                                                                │ │
│  │  Campo: patientId, patientData                                │ │
│  │  ¿Es nuevo?  → patientId = null, llenar patientData           │ │
│  │  ¿Existente? → patientId = <UUID>, patientData opcional       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ PASO 1: Datos Demográficos                                  │ │
│  │                                                                │ │
│  │  Campo: details                                               │ │
│  │  Sub-campos: birthDepartment, currentAddress,                 │ │
│  │    currentDistrict, currentDepartment, dniMatchesAddress,      │ │
│  │    travelTimeToHospital, emergencyContactName,                │ │
│  │    emergencyContactPhone, zoneType,                           │ │
│  │    emergencyContactGender, educationLevel,                     │ │
│  │    nativeLanguage, requiresTranslation                        │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ PASO 2: Seguro                                              │ │
│  │                                                                │ │
│  │  Campo: insurance                                             │ │
│  │  Sub-campos: insuranceType, epsProvider, isCurrent,           │ │
│  │    changeReason, startDate, endDate, contactId                │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ PASO 3: Signos y Síntomas                                   │ │
│  │                                                                │ │
│  │  Campo: symptomReport                                         │ │
│  │  Sub-campos: hasDiscomfort, signsAndSymptoms,                 │ │
│  │    hasSoughtMedicalConsultation, healthCenterId,              │ │
│  │    specialty, firstConsultationDetails,                       │ │
│  │    indicationsReceived                                        │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ PASO 4: Diagnóstico                                         │ │
│  │                                                                │ │
│  │  Campo: diagnosis                                             │ │
│  │  Sub-campos: diagnosis, cancerStage, diagnosisDate,           │ │
│  │    healthCenterId, diagnosisSpecialty,                        │ │
│  │    symptomLeadingToCheckup, waitTimeForDiagnosis,             │ │
│  │    hasMedicalReport, isCurrent, changeReason, contactId       │ │
│  └─────────────────────────────────────────────────────────────┘ │
│                              │                                    │
│                              ▼                                    │
│  ┌─────────────────────────────────────────────────────────────┐ │
│  │ PASO 5: Cierre                                              │ │
│  │                                                                │ │
│  │  Campos: treatment, medicalAppointments, sisAffiliation,      │ │
│  │    companions, enrollmentMetadata                             │ │
│  │                                                                │ │
│  │  Sub-pasos:                                                   │ │
│  │    a. Tratamiento (treatment)                                 │ │
│  │    b. Citas médicas (medicalAppointments)                     │ │
│  │    c. Afiliación SIS (sisAffiliation) — solo si no hay        │ │
│  │       seguro real (insurance = null o type = NONE)            │ │
│  │    d. Acompañantes (companions)                               │ │
│  │    e. Metadata (enrollmentMetadata): consentimientos,         │ │
│  │       comentarios, tiempos, agente                            │ │
│  └─────────────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────────┘
```

### Notas importantes para el frontend:

1. **`patientId` vs `patientData`**: Son mutuamente dependientes. Si `patientId` es `null`, `patientData` es **obligatorio**. Si `patientId` se envía, `patientData` es opcional y solo actualiza los campos enviados.

2. **`details` es obligatorio**: El endpoint requiere que `details` esté presente para completar la matriculación.

3. **`contactId` en sub-entidades**: Cada sub-entidad (insurance, diagnosis, treatment, appointments, sis) tiene su propio `contactId`. Estos deben ser IDs de Contactos existentes. En el flujo del wizard, normalmente serán IDs del mismo contacto de enrollment.

4. **SIS solo sin seguro real**: La afiliación SIS solo se procesa si el paciente no tiene seguro (`insurance` es `null` o `insuranceType = "NONE"`).

5. **Tratamiento requiere diagnóstico**: Si envías `treatment`, asegúrate de que `diagnosisId` referencie un diagnóstico existente. En la práctica del wizard, esto significa que el tratamiento debe enviarse **después** de que el diagnóstico esté creado (en una llamada separada, o el frontend debe coordinar los IDs).

6. **El Contacto de enrollment se maneja automáticamente**: No necesitas crear el Contact manualmente. El backend lo crea o actualiza basándose en `enrollmentMetadata`. Solo necesitas pasar los IDs de contacto para las sub-entidades si fueron creados previamente.

7. **El campo `contactId` del nivel raíz** de `FullEnrollmentRequest` no es utilizado por el backend actualmente. Puedes omitirlo o enviarlo como `null`.

---

## 7. Endpoints Relacionados (Post-Matriculación)

Una vez que el paciente está matriculado (`status = ENROLLED`), estos endpoints permiten agregar/modificar datos:

| Endpoint | Método | Descripción |
|----------|--------|-------------|
| `PUT /api/patients/{id}` | `PUT` | Actualizar datos básicos del paciente. |
| `PATCH /api/patients/{id}/status` | `PATCH` | Cambiar estado (ENROLLED → ACTIVE, etc.). |
| `PUT /api/patients/{id}/details` | `PUT` | Actualizar PatientDetails. |
| `GET /api/patients/{id}/insurance` | `GET` | Ver historial de seguros. |
| `POST /api/patients/{id}/insurance` | `POST` | Agregar nuevo seguro. |
| `GET /api/patients/{id}/diagnoses` | `GET` | Ver historial de diagnósticos. |
| `POST /api/patients/{id}/diagnoses` | `POST` | Agregar nuevo diagnóstico. |
| `GET /api/patients/{id}/treatments` | `GET` | Ver historial de tratamientos. |
| `POST /api/patients/{id}/treatments` | `POST` | Agregar nuevo tratamiento. |
| `GET /api/patients/{id}/appointments` | `GET` | Ver historial de citas. |
| `POST /api/patients/{id}/appointments` | `POST` | Agregar nueva cita. |
| `GET /api/patients/{id}/sis` | `GET` | Ver historial SIS. |
| `POST /api/patients/{id}/sis` | `POST` | Agregar registro SIS. |
| `PATCH /api/patients/{id}/sis/{sisId}/affiliate` | `PATCH` | Marcar SIS como afiliado. |
| `POST /api/patients/{id}/companions` | `POST` | Vincular acompañante. |
| `DELETE /api/patients/{id}/companions/{companionId}` | `DELETE` | Desvincular acompañante. |

---

*Documento generado a partir del código fuente del backend. Última actualización: mayo 2026.*
