# FPC Back — API Documentation

**Base URL:** `http://localhost:8080`

---

## Authentication

The API uses JWT-based authentication.

### Flow

1. **POST `/auth/login`** — Send email + password, receive an access token and a refresh token.
2. Include the access token in the `Authorization` header for all protected endpoints:
   ```
   Authorization: Bearer <accessToken>
   ```
3. Only access tokens can authenticate protected requests. Refresh tokens are only valid for **POST `/auth/refresh`**.
4. When the access token expires, call **POST `/auth/refresh`** with the refresh token to get a new pair.

### Auth Levels

| Level | Description |
|-------|-------------|
| **Public** | No authentication required |
| **JWT** | Any authenticated user with a valid access token |
| **ADMIN** | JWT + `ROLE_ADMIN` authority required (`@PreAuthorize("hasRole('ADMIN')")`) |

### CORS

Cross-origin requests are configurable through application properties.

| Property | Default |
|----------|---------|
| `APP_CORS_ALLOWED_ORIGIN_PATTERNS` | `http://localhost:3000,http://127.0.0.1:3000,http://localhost:5173,http://127.0.0.1:5173,https://*.vercel.app` |
| `APP_CORS_ALLOWED_METHODS` | `GET,POST,PUT,PATCH,DELETE,OPTIONS` |
| `APP_CORS_ALLOWED_HEADERS` | `Authorization,Content-Type,Accept,Origin` |
| `APP_CORS_EXPOSED_HEADERS` | empty |
| `APP_CORS_ALLOW_CREDENTIALS` | `false` |
| `APP_CORS_MAX_AGE_SECONDS` | `3600` |

> **Note:** CORS allows approved origins, but browsers still block mixed content. A frontend served over `https://` cannot call this API over plain `http://`.

### Auth-Level Summary by Domain

| Domain | Public | JWT | ADMIN |
|--------|--------|-----|-------|
| Auth | `/auth/login`, `/auth/refresh` | — | — |
| Users | — | `GET /users`, `GET /users/{id}` | `POST`, `PUT`, `DELETE /users` |
| Agents | — | `GET /agents`, `GET /agents/{id}` | `POST`, `PUT`, `DELETE /agents` |
| Patients | — | All endpoints | — |
| Contacts | — | `GET /api/contacts`, `GET /api/contacts/{id}` | `POST`, `PUT`, `DELETE /api/contacts` |
| Volunteers | — | `GET /api/volunteers`, `GET /api/volunteers/{id}` | `POST`, `PUT`, `DELETE /api/volunteers` |
| Volunteer Availability | — | `GET` availability slots | `POST`, `PUT`, `DELETE` slots |
| Psychooncology Appointments | — | `GET`, `POST .../complete`, `POST .../cancel` | `POST`, `PUT`, `DELETE` appointments |
| Health Centers | — | `GET`, `GET /slug/{slug}` | `POST`, `PUT`, `DELETE`, `PATCH .../reactivate` |
| Alerts | — | `GET` | `POST`, `PUT`, `DELETE`, `POST .../resolve` |
| Recordatorios | — | `GET` | `POST`, `PUT`, `DELETE`, `POST .../complete`, `POST .../cancel` |

> **Note:** `GET /agents` and `GET /agents/{id}` are JWT (no ADMIN restriction). All other `GET`-only list/detail endpoints across domains are JWT only unless otherwise noted.

---

## Enumerations

### PatientRole

| Value | Description |
|-------|-------------|
| `UNKNOWN` | Role not yet determined |
| `PATIENT` | The person is a patient |
| `COMPANION` | The person is a companion/caregiver |

### PatientStatus

| Value | Description |
|-------|-------------|
| `PROSPECT` | Initial status, not yet enrolled |
| `ENROLLED` | Patient details have been collected |
| `ACTIVE` | Currently receiving follow-up |
| `INACTIVE` | No longer in follow-up |

### EducationLevel

| Value |
|-------|
| `NONE` |
| `INITIAL` |
| `PRIMARY_INCOMPLETE` |
| `PRIMARY` |
| `SECONDARY_INCOMPLETE` |
| `SECONDARY` |
| `TECHNICAL_INCOMPLETE` |
| `TECHNICAL` |
| `HIGHER_INCOMPLETE` |
| `HIGHER` |

### ContactType

| Value | Description |
|-------|-------------|
| `WHATSAPP` | WhatsApp message/call |
| `CALL` | Phone call |
| `VIDEO_CALL` | Video call |
| `EMAIL` | Email communication |
| `IN_PERSON` | Face-to-face meeting |

### ContactStatus

| Value |
|-------|
| `SCHEDULED` |
| `COMPLETED` |
| `CANCELLED` |
| `NO_ANSWER` |

### ContactPurpose

| Value | Description |
|-------|-------------|
| `FIRST_CONTACT` | Initial outreach |
| `ENROLLMENT` | Enrollment-related |
| `FOLLOW_UP` | Follow-up communication |
| `PSYCHOONCOLOGY_REFERRAL` | Referral to psychooncology |
| `OTHER` | Other purpose |

### InsuranceType

| Value | Description |
|-------|-------------|
| `SIS` | Seguro Integral de Salud |
| `ESSALUD` | Seguro Social de Salud |
| `EPS` | Entidad Prestadora de Salud |
| `FUERZAS_ARMADAS` | Armed Forces insurance |
| `SALUDPOL` | National Police insurance |
| `NONE` | No insurance |

### EpsProvider

| Value |
|-------|
| `PACIFICO` |
| `RIMAC` |
| `MAPFRE` |
| `LA_POSITIVA` |
| `SANITAS` |
| `ONCOSALUD` |
| `OTHER` |

### CancerStage

| Value |
|-------|
| `STAGE_1` |
| `STAGE_2` |
| `STAGE_3` |
| `STAGE_4` |
| `UNKNOWN` |

### AlertStatus

| Value |
|-------|
| `ACTIVE` |
| `RESOLVED` |

### AvailabilityStatus

| Value |
|-------|
| `AVAILABLE` |
| `RESERVED` |

### AppointmentModality

| Value |
|-------|
| `CALL` |
| `VIDEO_CALL` |

### AppointmentStatus

| Value |
|-------|
| `SCHEDULED` |
| `COMPLETED` |
| `CANCELLED` |
| `NO_ANSWER` |

### ReminderType

| Value | Description |
|-------|-------------|
| `LABORATORIO` | Lab test reminder |
| `IMAGEN` | Imaging study reminder |
| `CONSULTA` | Medical consultation reminder |
| `PROCEDIMIENTO` | Medical procedure reminder |
| `MEDICACION` | Medication reminder |
| `OTRO` | Other type of reminder |

### ReminderStatus

| Value | Description |
|-------|-------------|
| `PENDIENTE` | Pending (default) |
| `COMPLETADO` | Completed |
| `CANCELADO` | Canceled |

### ReferralType

| Value | Description |
|-------|-------------|
| `PSYCHIATRY` | Referred to psychiatry |
| `NEUROLOGY` | Referred to neurology |
| `CONTINUE_PSYCHOLOGY` | Continue with general psychology |
| `PSYCHOONCOLOGIST` | Referred to another psychooncologist |
| `NONE` | No referral needed |

### UserRole

| Value |
|-------|
| `ADMIN` |
| `AGENT` |
| `VOLUNTEER` |

> **Note:** The UserRole enum has `AGENT` (not `CALLCENTER`). The domain-specific Agent entity maps to UserRole `AGENT`. There is no `CALLCENTER` role — this was a V3 naming inconsistency that has been resolved.

### PeruDepartment (25 departments + 1 constitutional province)

| # | Value |
|---|-------|
| 1 | `AMAZONAS` |
| 2 | `ANCASH` |
| 3 | `APURIMAC` |
| 4 | `AREQUIPA` |
| 5 | `AYACUCHO` |
| 6 | `CAJAMARCA` |
| 7 | `CALLAO` |
| 8 | `CUSCO` |
| 9 | `HUANCAVELICA` |
| 10 | `HUANUCO` |
| 11 | `ICA` |
| 12 | `JUNIN` |
| 13 | `LA_LIBERTAD` |
| 14 | `LAMBAYEQUE` |
| 15 | `LIMA` |
| 16 | `LORETO` |
| 17 | `MADRE_DE_DIOS` |
| 18 | `MOQUEGUA` |
| 19 | `PASCO` |
| 20 | `PIURA` |
| 21 | `PUNO` |
| 22 | `SAN_MARTIN` |
| 23 | `TACNA` |
| 24 | `TUMBES` |
| 25 | `UCAYALI` |

---

## Endpoints

---

### 1. Auth

#### POST `/auth/login` — Public

Authenticate with email and password.

**Request body:**
```json
{
  "email": "string",
  "password": "string"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "string (JWT)",
  "refreshToken": "string (JWT)",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "role": "AGENT",
    "isActive": true,
    "createdAt": "2025-01-15T10:30:00Z",
    "updatedAt": "2025-01-15T10:30:00Z"
  }
}
```

**Status codes:** `200` — Success | `401` — Invalid credentials

---

#### POST `/auth/refresh` — Public

Refresh an expired access token using a valid refresh token.

**Request body:**
```json
{
  "refreshToken": "string"
}
```

**Response `200 OK`:**
```json
{
  "accessToken": "string (new JWT)",
  "refreshToken": "string (new JWT)",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "user@example.com",
    "role": "AGENT",
    "isActive": true,
    "createdAt": "2025-01-15T10:30:00Z",
    "updatedAt": "2025-01-15T10:30:00Z"
  }
}
```

**Status codes:** `200` — Success | `401` — Invalid/expired refresh token

---

### 2. Users

All endpoints under `/users`. Role: `ADMIN` for write operations.

#### GET `/users` — JWT

List all users (paginated).

**Query parameters (Spring Pageable):** `page`, `size`, `sort`

**Response `200 OK`:**
```json
{
  "content": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "user@example.com",
      "role": "AGENT",
      "isActive": true,
      "createdAt": "2025-01-15T10:30:00Z",
      "updatedAt": "2025-01-15T10:30:00Z"
    }
  ],
  "totalElements": 1,
  "totalPages": 1,
  "size": 20,
  "number": 0
}
```

**Status codes:** `200` — Success

---

#### GET `/users/{id}` — JWT

Get a single user by UUID.

**Path parameters:** `id` — UUID

**Response `200 OK`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "role": "AGENT",
  "isActive": true,
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

**Status codes:** `200` — Success | `404` — Not found

---

#### POST `/users` — ADMIN

Create a new user.

**Request body:**
```json
{
  "email": "newuser@example.com",
  "password": "securePassword123",
  "role": "AGENT"
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "newuser@example.com",
  "role": "AGENT",
  "isActive": true,
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-01-15T10:30:00Z"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `409` — Email already exists

---

#### PUT `/users/{id}` — ADMIN

Update an existing user. All fields are optional — only provided fields are updated.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "email": "updated@example.com",
  "password": "newPassword123",
  "role": "VOLUNTEER",
  "isActive": false
}
```

**Response `200 OK`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "updated@example.com",
  "role": "VOLUNTEER",
  "isActive": false,
  "createdAt": "2025-01-15T10:30:00Z",
  "updatedAt": "2025-06-01T14:00:00Z"
}
```

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/users/{id}` — ADMIN

Soft-delete or deactivate a user.

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

### 3. Agents

All endpoints under `/agents`.

#### POST `/agents` — ADMIN

Create a new agent.

**Request body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "Juan Pérez",
  "phone": "+51999000111"
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440001",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "Juan Pérez",
  "phone": "+51999000111",
  "createdAt": "2025-01-15T10:30:00Z"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `404` — User not found

---

#### GET `/agents` — JWT

List all agents.

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440001",
    "userId": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "Juan Pérez",
    "phone": "+51999000111",
    "createdAt": "2025-01-15T10:30:00Z"
  }
]
```

**Status codes:** `200` — Success

---

#### GET `/agents/{id}` — JWT

Get a single agent by UUID.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Same shape as list response.

**Status codes:** `200` — Success | `404` — Not found

---

#### PUT `/agents/{id}` — ADMIN

Update an agent. All fields are optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440002",
  "fullName": "Juan Updated",
  "phone": "+51999000222"
}
```

**Response `200 OK`:** Updated AgentResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/agents/{id}` — ADMIN

Delete an agent.

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

### 4. Patients

All endpoints under `/api/patients`. This is the largest and most important domain.

**Patient flow:**
1. **Create** a basic patient (`POST /api/patients`) → status `PROSPECT`
2. **Enroll** the patient (`POST /api/patients/enroll`) → status `ENROLLED`
3. Add or update **details**, **insurance**, **diagnoses**, **treatments**, **appointments**, **SIS**, **companions**, **family prevention talk interests**, and **contact service referrals**
4. Manage patient through statuses: `PROSPECT` → `ENROLLED` → `ACTIVE` / `INACTIVE`

> **Note:** Some `patient_details` fields are intentionally filled after enrollment through `PUT /api/patients/{id}/details`, such as follow-up social data, dropout data, and deceased status.

---

#### GET `/api/patients` — JWT

List all patients with full detail.

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "fullName": "María García",
    "dni": "12345678",
    "birthDate": "1985-03-15",
    "primaryPhone": "+51999000333",
    "secondaryPhone": "+51999000444",
    "hasWhatsapp": true,
    "role": "PATIENT",
    "status": "ENROLLED",
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-06-01T14:00:00",
    "details": null,
    "insurance": [],
    "diagnoses": [],
    "treatments": [],
    "medicalAppointments": [],
    "sisAffiliations": [],
    "companions": [],
    "familyPreventionTalkInterests": [],
    "contacts": []
  }
]
```

**Status codes:** `200` — Success

---

#### GET `/api/patients/status/{status}` — JWT

Filter patients by status.

**Path parameters:** `status` — PatientStatus enum (`PROSPECT`, `ENROLLED`, `ACTIVE`, `INACTIVE`)

**Response `200 OK`:** List of PatientResponse.

**Status codes:** `200` — Success

---

#### GET `/api/patients/role/{role}` — JWT

Filter patients by role.

**Path parameters:** `role` — PatientRole enum (`UNKNOWN`, `PATIENT`, `COMPANION`)

**Response `200 OK`:** List of PatientResponse.

**Status codes:** `200` — Success

---

#### GET `/api/patients/{id}` — JWT

Get a single patient with ALL related data (details, insurance, diagnoses, treatments, appointments, SIS, companions, family prevention talk interests, contacts).

**Path parameters:** `id` — UUID

**Response `200 OK`:** Single PatientResponse (see full structure below).

**Status codes:** `200` — Success | `404` — Not found

---

#### POST `/api/patients` — JWT

Create a new basic patient. Status defaults to `PROSPECT`, role defaults to `UNKNOWN`.

**Request body:**
```json
{
  "fullName": "María García",
  "dni": "12345678",
  "birthDate": "1985-03-15",
  "primaryPhone": "+51999000333",
  "secondaryPhone": "+51999000444",
  "hasWhatsapp": true,
  "role": "PATIENT",
  "status": null
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "María García",
  "dni": "12345678",
  "birthDate": "1985-03-15",
  "primaryPhone": "+51999000333",
  "secondaryPhone": "+51999000444",
  "hasWhatsapp": true,
  "role": "PATIENT",
  "status": "PROSPECT",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00",
  "details": null,
  "insurance": [],
  "diagnoses": [],
  "treatments": [],
  "medicalAppointments": [],
  "sisAffiliations": [],
    "companions": [],
    "familyPreventionTalkInterests": [],
    "contacts": []
}
```

**Status codes:** `201` — Created | `400` — Validation error

---

#### PUT `/api/patients/{id}` — JWT

Update the basic fields of a patient. All fields are optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "fullName": "María García Actualizada",
  "dni": "87654321",
  "birthDate": "1985-06-20",
  "primaryPhone": "+51999000555",
  "secondaryPhone": null,
  "hasWhatsapp": false,
  "role": "COMPANION"
}
```

**Response `200 OK`:** Updated PatientResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### PATCH `/api/patients/{id}/status` — JWT

Change only the patient's status (e.g., from `PROSPECT` to `ENROLLED` or `ACTIVE`).

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "newStatus": "ENROLLED"
}
```

**Response `200 OK`:** Updated PatientResponse with new status.

**Status codes:** `200` — Success | `404` — Not found

---

#### POST `/api/patients/enroll` — JWT ★ KEY ENDPOINT

**Full Enrollment** — Atomically creates or enrolls a patient with all related data in a single transaction. A contact is auto-created during enrollment and injected into all sub-entities — no `contactId` input is needed.

This is the primary endpoint for the agent workflow. It handles two scenarios:
- **New patient** (`patientId: null` + `patientData` provided) → creates patient + enrolls
- **Existing patient** (`patientId: UUID` provided) → enrolls the existing patient

**Request body — Complete example:**
```json
{
  "patientId": null,
  "patientData": {
    "fullName": "María García",
    "dni": "12345678",
    "birthDate": "1985-03-15",
    "primaryPhone": "+51999000333",
    "secondaryPhone": "+51999000444",
    "hasWhatsapp": true,
    "role": "PATIENT",
    "status": null
  },
  "details": {
    "birthDepartment": "LIMA",
    "currentAddress": "Av. Principal 123",
    "currentDistrict": "Miraflores",
    "currentDepartment": "LIMA",
    "dniMatchesAddress": true,
    "travelTimeToHospital": "30 minutos",
    "emergencyContactName": "Carlos García",
    "emergencyContactPhone": "+51999000555",
    "educationLevel": "SECONDARY",
    "nativeLanguage": "Quechua",
    "requiresTranslation": true
  },
  "insurance": {
    "insuranceType": "SIS",
    "epsProvider": null,
    "isCurrent": true,
    "changeReason": "Primer registro",
    "startDate": "2025-01-01",
    "endDate": null
  },
  "diagnosis": {
    "diagnosis": "Cáncer de mama",
    "cancerStage": "STAGE_2",
    "diagnosisDate": "2024-11-01",
    "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
    "diagnosisSpecialty": "Oncología",
    "symptomLeadingToCheckup": "Bulto en seno izquierdo",
    "waitTimeForDiagnosis": "2 meses",
    "hasMedicalReport": true,
    "isCurrent": true,
    "changeReason": "Diagnóstico inicial"
  },
  "treatment": {
    "diagnosisId": "00000000-0000-0000-0000-000000000000",
    "treatmentType": "Quimioterapia",
    "treatmentFrequency": "Mensual",
    "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
    "startDate": "2025-01-15",
    "endDate": null,
    "isCurrent": true,
    "changeReason": "Plan de tratamiento inicial",
    "notReceivingReason": null,
    "treatmentSituation": "EN_PROCESO"
  },
  "medicalAppointments": [
    {
      "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
      "specialty": "Oncología",
      "appointmentDate": "2025-02-01",
      "nextAppointmentDate": "2025-03-01",
      "hasReferralSheet": true,
      "referredTo": "Hospital Nacional",
      "difficulties": "Ninguna"
    }
  ],
  "familyPreventionTalkInterests": [
    {
      "talkName": "Prevencion del cancer de mama",
      "familyMemberName": "Rosa Garcia",
      "familyMemberPhone": "+51999000777",
      "familyMemberEmail": "rosa@example.com"
    }
  ],
  "sisAffiliation": {
    "canAffiliate": true,
    "expectedDate": "2025-02-15",
    "cantAffiliateReason": null
  },
  "companions": [
    {
      "companionId": "550e8400-e29b-41d4-a716-446655440030",
      "isPrimaryInformant": true
    }
  ]
}
```

**Request body fields:**

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `patientId` | UUID or `null` | Yes | `null` for new patient; UUID to enroll existing |
| `patientData` | CreatePatientRequest | If `patientId` is null | Basic patient data |
| `details` | EnrollPatientDetailsRequest | No | Patient details for enrollment |
| `insurance` | AddInsuranceRequest | No | Insurance record |
| `diagnosis` | AddDiagnosisRequest | No | Diagnosis record |
| `treatment` | AddTreatmentRequest | No | Treatment record (requires diagnosis) |
| `medicalAppointments` | List of AddMedicalAppointmentRequest | No | Medical appointments |
| `familyPreventionTalkInterests` | List of FamilyPreventionTalkInterestRequest | No | Interested family members for cancer prevention talks |
| `sisAffiliation` | AddSisAffiliationRequest | No | SIS affiliation (processed when no real insurance exists) |
| `companions` | List of LinkCompanionRequest | No | Companions to link |

**Response `201 Created`:** Full PatientResponse (see complete structure below).

**Business rules:**
- If `patientId` is provided (existing patient), `patientData` is optional — if provided, it updates the existing patient's basic data.
- If `patientId` is `null`, `patientData` is required.
- The `details` sub-object triggers enrollment (status change from `PROSPECT` to `ENROLLED`).
- The `treatment.diagnosisId` can be left as `null` — the service will link the treatment to the newly created diagnosis if both are provided together.
- `sisAffiliation` is only processed if the patient has no real insurance (i.e., `insurance.insuranceType` is not provided or is `NONE`).
- `companions` must already exist as patients with role `COMPANION`.
- `familyPreventionTalkInterests` stores zero or more family contacts linked 1:N to the patient.
- No `contactId` is required anywhere in the request — the service auto-creates a contact and injects it into all sub-entities (insurance, diagnosis, treatment, appointments, SIS).
- `enrollmentMetadata` is optional — a minimal COMPLETED contact is still created even without it.

**Status codes:** `201` — Created | `400` — Validation error | `404` — Referenced entity not found

---

#### PUT `/api/patients/{id}/details` — JWT

Update an existing patient's details. All fields are optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "birthDepartment": "CALLAO",
  "currentAddress": "Av. Nueva 456",
  "currentDistrict": "Callao",
  "currentDepartment": "CALLAO",
  "dniMatchesAddress": false,
  "travelTimeToHospital": "15 minutos",
  "emergencyContactName": "Ana García",
  "emergencyContactPhone": "+51999000666",
  "zoneType": "URBANA",
  "emergencyContactGender": "F",
  "educationLevel": "HIGHER",
  "nativeLanguage": "Aymara",
  "requiresTranslation": true,
  "evidenceOfDomesticViolence": false,
  "usesWoodStove": true,
  "isWorking": true,
  "receivesFinancialSupport": false,
  "programDropoutReason": null,
  "programDropoutDate": null,
  "referredToSocialWorker": true,
  "hasConadisCard": false,
  "knowsAboutFissal": true,
  "isDeceased": false
}
```

These follow-up fields are optional and nullable:
- `evidenceOfDomesticViolence`
- `usesWoodStove`
- `isWorking`
- `receivesFinancialSupport`
- `programDropoutReason`
- `programDropoutDate`
- `referredToSocialWorker`
- `hasConadisCard`
- `knowsAboutFissal`
- `isDeceased`

**Response `200 OK`:** Updated PatientResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### GET `/api/patients/{id}/insurance` — JWT

Get insurance history for a patient.

**Path parameters:** `id` — UUID

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440040",
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "insuranceType": "SIS",
    "epsProvider": null,
    "isCurrent": true,
    "changeReason": "Primer registro",
    "startDate": "2025-01-01",
    "endDate": null,
    "createdAt": "2025-01-15T10:30:00",
    "contact": {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "agentName": "Juan Pérez",
      "type": "CALL",
      "status": "COMPLETED",
      "purpose": "ENROLLMENT"
    }
  }
]
```

**Status codes:** `200` — Success | `404` — Patient not found

---

#### POST `/api/patients/{id}/insurance` — JWT

Add an insurance record for a patient.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "insuranceType": "EPS",
  "epsProvider": "RIMAC",
  "isCurrent": true,
  "changeReason": "Cambio de seguro",
  "startDate": "2025-03-01",
  "endDate": null,
  "contactId": "550e8400-e29b-41d4-a716-446655440010"
}
```

**Response `201 Created`:** Updated PatientResponse with new insurance record.

**Status codes:** `201` — Created | `404` — Patient/Contact not found

---

#### GET `/api/patients/{id}/diagnoses` — JWT

Get diagnosis history for a patient.

**Path parameters:** `id` — UUID

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440050",
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "diagnosis": "Cáncer de mama",
    "cancerStage": "STAGE_2",
    "diagnosisDate": "2024-11-01",
    "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
    "healthCenterName": "Hospital Nacional",
    "diagnosisSpecialty": "Oncología",
    "symptomLeadingToCheckup": "Bulto en seno izquierdo",
    "waitTimeForDiagnosis": "2 meses",
    "hasMedicalReport": true,
    "isCurrent": true,
    "changeReason": "Diagnóstico inicial",
    "createdAt": "2025-01-15T10:30:00",
    "contact": {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "agentName": "Juan Pérez",
      "type": "CALL",
      "status": "COMPLETED",
      "purpose": "ENROLLMENT"
    }
  }
]
```

**Status codes:** `200` — Success | `404` — Patient not found

---

#### POST `/api/patients/{id}/diagnoses` — JWT

Add a diagnosis record for a patient.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "diagnosis": "Cáncer de mama",
  "cancerStage": "STAGE_2",
  "diagnosisDate": "2024-11-01",
  "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
  "diagnosisSpecialty": "Oncología",
  "symptomLeadingToCheckup": "Bulto en seno izquierdo",
  "waitTimeForDiagnosis": "2 meses",
  "hasMedicalReport": true,
  "isCurrent": true,
  "changeReason": "Diagnóstico inicial",
  "contactId": "550e8400-e29b-41d4-a716-446655440010"
}
```

**Response `201 Created`:** Updated PatientResponse with new diagnosis record.

**Status codes:** `201` — Created | `404` — Patient/Contact/HealthCenter not found

---

#### GET `/api/patients/{id}/treatments` — JWT

Get treatment history for a patient.

**Path parameters:** `id` — UUID

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440060",
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "diagnosis": {
      "id": "550e8400-e29b-41d4-a716-446655440050",
      "diagnosis": "Cáncer de mama"
    },
    "treatmentType": "Quimioterapia",
    "treatmentFrequency": "Mensual",
    "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
    "healthCenterName": "Hospital Nacional",
    "startDate": "2025-01-15",
    "endDate": null,
    "isCurrent": true,
    "changeReason": "Plan de tratamiento inicial",
    "notReceivingReason": null,
    "treatmentSituation": null,
    "createdAt": "2025-01-15T10:30:00",
    "contact": {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "agentName": "Juan Pérez",
      "type": "CALL",
      "status": "COMPLETED",
      "purpose": "ENROLLMENT"
    }
  }
]

**Status codes:** `200` — Success | `404` — Patient not found

---

#### POST `/api/patients/{id}/treatments` — JWT

Add a treatment record for a patient.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "diagnosisId": "550e8400-e29b-41d4-a716-446655440050",
  "treatmentType": "Quimioterapia",
  "treatmentFrequency": "Mensual",
  "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
  "startDate": "2025-01-15",
  "endDate": null,
  "isCurrent": true,
  "changeReason": "Plan de tratamiento inicial",
  "notReceivingReason": null,
  "treatmentSituation": "EN_PROCESO",
  "contactId": "550e8400-e29b-41d4-a716-446655440010"
}
```

**Response `201 Created`:** Updated PatientResponse with new treatment record.

**Status codes:** `201` — Created | `404` — Patient/Diagnosis/Contact not found

---

### PatientResponse — Complete Structure

This is the full response shape returned by most patient endpoints:

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "fullName": "María García",
  "dni": "12345678",
  "birthDate": "1985-03-15",
  "primaryPhone": "+51999000333",
  "secondaryPhone": "+51999000444",
  "hasWhatsapp": true,
  "role": "PATIENT",
  "status": "ENROLLED",
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-06-01T14:00:00",
  "details": {
    "id": "550e8400-e29b-41d4-a716-446655440090",
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "birthDepartment": "LIMA",
    "currentAddress": "Av. Principal 123",
    "currentDistrict": "Miraflores",
    "currentDepartment": "LIMA",
    "dniMatchesAddress": true,
    "travelTimeToHospital": "30 minutos",
    "emergencyContactName": "Carlos García",
    "emergencyContactPhone": "+51999000555",
    "zoneType": "URBANA",
    "emergencyContactGender": "M",
    "educationLevel": "SECONDARY",
    "nativeLanguage": "Quechua",
    "requiresTranslation": true,
    "evidenceOfDomesticViolence": false,
    "usesWoodStove": true,
    "isWorking": false,
    "receivesFinancialSupport": true,
    "programDropoutReason": null,
    "programDropoutDate": null,
    "referredToSocialWorker": true,
    "hasConadisCard": false,
    "knowsAboutFissal": true,
    "isDeceased": false,
    "createdAt": "2025-01-15T10:30:00",
    "updatedAt": "2025-01-15T10:30:00"
  },
  "insurance": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440040",
      "patientId": "550e8400-e29b-41d4-a716-446655440000",
      "insuranceType": "SIS",
      "epsProvider": null,
      "isCurrent": true,
      "changeReason": "Primer registro",
      "startDate": "2025-01-01",
      "endDate": null,
      "createdAt": "2025-01-15T10:30:00",
      "contact": {
        "id": "550e8400-e29b-41d4-a716-446655440010",
        "agentName": "Juan Pérez",
        "type": "CALL",
        "status": "COMPLETED",
        "purpose": "ENROLLMENT"
      }
    }
  ],
  "diagnoses": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440050",
      "patientId": "550e8400-e29b-41d4-a716-446655440000",
      "diagnosis": "Cáncer de mama",
      "cancerStage": "STAGE_2",
      "diagnosisDate": "2024-11-01",
      "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
      "healthCenterName": "Hospital Nacional",
      "diagnosisSpecialty": "Oncología",
      "symptomLeadingToCheckup": "Bulto en seno izquierdo",
      "waitTimeForDiagnosis": "2 meses",
      "hasMedicalReport": true,
      "isCurrent": true,
      "changeReason": "Diagnóstico inicial",
      "createdAt": "2025-01-15T10:30:00",
      "contact": {
        "id": "550e8400-e29b-41d4-a716-446655440010",
        "agentName": "Juan Pérez",
        "type": "CALL",
        "status": "COMPLETED",
        "purpose": "ENROLLMENT"
      }
    }
  ],
  "treatments": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440060",
      "patientId": "550e8400-e29b-41d4-a716-446655440000",
      "diagnosis": {
        "id": "550e8400-e29b-41d4-a716-446655440050",
        "diagnosis": "Cáncer de mama"
      },
      "treatmentType": "Quimioterapia",
      "treatmentFrequency": "Mensual",
      "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
      "healthCenterName": "Hospital Nacional",
      "startDate": "2025-01-15",
      "endDate": null,
      "isCurrent": true,
      "changeReason": "Plan de tratamiento inicial",
      "notReceivingReason": null,
      "treatmentSituation": "EN_PROCESO",
      "createdAt": "2025-01-15T10:30:00",
      "contact": {
        "id": "550e8400-e29b-41d4-a716-446655440010",
        "agentName": "Juan Pérez",
        "type": "CALL",
        "status": "COMPLETED",
        "purpose": "ENROLLMENT"
      }
    }
  ],
  "medicalAppointments": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440070",
      "patientId": "550e8400-e29b-41d4-a716-446655440000",
      "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
      "healthCenterName": "Hospital Nacional",
      "specialty": "Oncología",
      "appointmentDate": "2025-02-01",
      "nextAppointmentDate": "2025-03-01",
      "hasReferralSheet": true,
      "referredTo": "Hospital Nacional",
      "difficulties": "Ninguna",
      "createdAt": "2025-01-15T10:30:00",
      "contact": {
        "id": "550e8400-e29b-41d4-a716-446655440010",
        "agentName": "Juan Pérez",
        "type": "CALL",
        "status": "COMPLETED",
        "purpose": "ENROLLMENT"
      }
    }
  ],
  "sisAffiliations": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440080",
      "patientId": "550e8400-e29b-41d4-a716-446655440000",
      "contactId": "550e8400-e29b-41d4-a716-446655440010",
      "canAffiliate": true,
      "expectedDate": "2025-02-15",
      "cantAffiliateReason": null,
      "affiliatedAt": "2025-02-10T09:00:00",
      "createdAt": "2025-01-15T10:30:00"
    }
  ],
  "companions": [
    {
      "companionId": "550e8400-e29b-41d4-a716-446655440030",
      "companionFullName": "Carlos García",
      "isPrimaryInformant": true
    }
  ],
  "familyPreventionTalkInterests": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440081",
      "patientId": "550e8400-e29b-41d4-a716-446655440000",
      "talkName": "Prevencion del cancer de mama",
      "familyMemberName": "Rosa Garcia",
      "familyMemberPhone": "+51999000777",
      "familyMemberEmail": "rosa@example.com",
      "createdAt": "2025-01-15T10:30:00"
    }
  ],
  "contacts": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440010",
      "agentName": "Juan Pérez",
      "type": "CALL",
      "status": "COMPLETED",
      "purpose": "ENROLLMENT",
      "scheduledAt": "2025-01-10T14:00:00",
      "completedAt": "2025-01-10T14:30:00",
      "notes": "Paciente contactada exitosamente",
      "serviceReferral": {
        "id": "550e8400-e29b-41d4-a716-446655440011",
        "contactId": "550e8400-e29b-41d4-a716-446655440010",
        "referredToSocialWorker": true,
        "referredToSusalud": false,
        "susaludRegistrationNumber": null,
        "receivedFoodGuide": true,
        "participatesInGam": false,
        "programSatisfaction": "Agradece el acompanamiento recibido.",
        "wellbeingChanges": "Refiere menos ansiedad y mejor organizacion familiar.",
        "knowsAboutFissal": true,
        "referredToPaus": false,
        "referredToDae": false,
        "referredToFissal": false,
        "createdAt": "2025-01-10T14:30:00",
        "updatedAt": "2025-01-10T14:30:00"
      },
      "createdAt": "2025-01-10T14:00:00"
    }
  ]
}
```

#### PatientResponse Field Reference

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Patient unique identifier |
| `fullName` | String | Full name |
| `dni` | String or null | National ID (DNI) |
| `birthDate` | LocalDate or null | Date of birth (ISO 8601: `yyyy-MM-dd`) |
| `primaryPhone` | String | Primary phone number |
| `secondaryPhone` | String or null | Secondary phone number |
| `hasWhatsapp` | Boolean | Whether the patient uses WhatsApp |
| `role` | PatientRole | PatientRole enum |
| `status` | PatientStatus | PatientStatus enum |
| `createdAt` | LocalDateTime | Timestamp (ISO 8601: `yyyy-MM-ddTHH:mm:ss`) |
| `updatedAt` | LocalDateTime | Last updated timestamp |
| `details` | PatientDetailsResponse or null | Enrolled patient details |
| `insurance` | List of InsuranceRecordResponse | Insurance history |
| `diagnoses` | List of DiagnosisRecordResponse | Diagnosis history |
| `treatments` | List of TreatmentRecordResponse | Treatment history |
| `medicalAppointments` | List of MedicalAppointmentResponse | Medical appointments |
| `sisAffiliations` | List of SisAffiliationResponse | SIS affiliation records |
| `companions` | List of CompanionResponse | Linked companions |
| `familyPreventionTalkInterests` | List of FamilyPreventionTalkInterestResponse | Interested family members for prevention talks |
| `contacts` | List of ContactResponse | Contact history |

#### PatientDetailsResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Details record ID |
| `patientId` | UUID | Patient ID |
| `birthDepartment` | String or null | Birth department |
| `currentAddress` | String or null | Current address |
| `currentDistrict` | String or null | Current district |
| `currentDepartment` | String or null | Current department |
| `dniMatchesAddress` | Boolean or null | Whether DNI address matches current |
| `travelTimeToHospital` | String or null | Travel time to hospital |
| `emergencyContactName` | String or null | Emergency contact name |
| `emergencyContactPhone` | String or null | Emergency contact phone |
| `zoneType` | String or null | Urban/rural zone descriptor |
| `emergencyContactGender` | String or null | Emergency contact gender |
| `educationLevel` | EducationLevel or null | Education level |
| `nativeLanguage` | String or null | Native language |
| `requiresTranslation` | Boolean | Whether translation is needed |
| `evidenceOfDomesticViolence` | Boolean or null | Whether domestic violence is evidenced |
| `usesWoodStove` | Boolean or null | Whether the household uses a wood-burning stove |
| `isWorking` | Boolean or null | Whether the person is currently working |
| `receivesFinancialSupport` | Boolean or null | Whether the person receives financial support |
| `programDropoutReason` | String or null | Reason for dropping out of the program |
| `programDropoutDate` | LocalDate or null | Date of program dropout |
| `referredToSocialWorker` | Boolean or null | Whether the patient was referred to a social worker |
| `hasConadisCard` | Boolean or null | Whether the patient has a CONADIS card |
| `knowsAboutFissal` | Boolean or null | Whether the patient knows about FISSAL |
| `isDeceased` | Boolean or null | Whether the person is deceased |
| `createdAt` | LocalDateTime | Timestamp |
| `updatedAt` | LocalDateTime | Last updated |

#### InsuranceRecordResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Insurance record ID |
| `patientId` | UUID | Patient ID |
| `insuranceType` | InsuranceType | Type of insurance |
| `epsProvider` | EpsProvider or null | EPS provider (if EPS type) |
| `isCurrent` | Boolean | Whether this is the current insurance |
| `changeReason` | String or null | Reason for change |
| `startDate` | LocalDate or null | Start date |
| `endDate` | LocalDate or null | End date |
| `createdAt` | LocalDateTime | Timestamp |
| `contact` | ContactSummary | Contact that recorded this |

#### DiagnosisRecordResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Diagnosis record ID |
| `patientId` | UUID | Patient ID |
| `diagnosis` | String | Diagnosis name |
| `cancerStage` | CancerStage or null | Cancer stage |
| `diagnosisDate` | LocalDate or null | Date of diagnosis |
| `healthCenterId` | UUID or null | Health center ID |
| `healthCenterName` | String or null | Health center name |
| `diagnosisSpecialty` | String or null | Medical specialty |
| `symptomLeadingToCheckup` | String or null | Initial symptom |
| `waitTimeForDiagnosis` | String or null | Wait time description |
| `hasMedicalReport` | Boolean | Whether medical report exists |
| `isCurrent` | Boolean | Whether this is current diagnosis |
| `changeReason` | String or null | Reason for change |
| `createdAt` | LocalDateTime | Timestamp |
| `contact` | ContactSummary | Contact that recorded this |

#### TreatmentRecordResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Treatment record ID |
| `patientId` | UUID | Patient ID |
| `diagnosis` | DiagnosisSummary | Linked diagnosis (id + name) |
| `treatmentType` | String | Type of treatment |
| `treatmentFrequency` | String or null | Frequency description |
| `healthCenterId` | UUID or null | Health center ID |
| `healthCenterName` | String or null | Health center name |
| `startDate` | LocalDate or null | Treatment start date |
| `endDate` | LocalDate or null | Treatment end date |
| `isCurrent` | Boolean | Whether currently receiving |
| `changeReason` | String or null | Reason for change |
| `notReceivingReason` | String or null | Reason if not receiving treatment |
| `treatmentSituation` | String or null | Current treatment situation |
| `createdAt` | LocalDateTime | Timestamp |
| `contact` | ContactSummary | Contact that recorded this |

#### MedicalAppointmentResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Appointment record ID |
| `patientId` | UUID | Patient ID |
| `healthCenterId` | UUID or null | Health center ID |
| `healthCenterName` | String or null | Health center name |
| `specialty` | String or null | Medical specialty |
| `appointmentDate` | LocalDate or null | Appointment date |
| `nextAppointmentDate` | LocalDate or null | Next scheduled appointment |
| `hasReferralSheet` | Boolean | Whether referral sheet exists |
| `referredTo` | String or null | Referred destination |
| `difficulties` | String or null | Difficulties encountered |
| `createdAt` | LocalDateTime | Timestamp |
| `contact` | ContactSummary | Contact that recorded this |

#### SisAffiliationResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | SIS record ID |
| `patientId` | UUID | Patient ID |
| `contactId` | UUID | Contact ID |
| `canAffiliate` | Boolean | Whether patient can be affiliated |
| `expectedDate` | LocalDate or null | Expected affiliation date |
| `cantAffiliateReason` | String or null | Reason if cannot affiliate |
| `affiliatedAt` | LocalDateTime or null | Actual affiliation timestamp |
| `createdAt` | LocalDateTime | Timestamp |

#### CompanionResponse

| Field | Type | Description |
|-------|------|-------------|
| `companionId` | UUID | Companion patient ID |
| `companionFullName` | String | Companion's full name |
| `isPrimaryInformant` | Boolean | Whether this is the primary informant |

#### FamilyPreventionTalkInterestResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Interest record ID |
| `patientId` | UUID | Patient ID |
| `talkName` | String | Prevention talk topic requested by the family |
| `familyMemberName` | String | Interested family member name |
| `familyMemberPhone` | String | Interested family member phone |
| `familyMemberEmail` | String | Interested family member email |
| `createdAt` | LocalDateTime | Timestamp |

#### ContactResponse (patient context — simplified)

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Contact ID |
| `agentName` | String or null | Agent's full name |
| `type` | ContactType | Type of contact |
| `status` | ContactStatus | Contact status |
| `purpose` | ContactPurpose | Purpose of contact |
| `scheduledAt` | LocalDateTime or null | Scheduled date/time |
| `completedAt` | LocalDateTime or null | Completion date/time |
| `notes` | String or null | Notes from contact |
| `serviceReferral` | ContactServiceReferralResponse or null | Optional services/referrals captured for the contact |
| `createdAt` | LocalDateTime | Timestamp |

#### ContactServiceReferralResponse

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Service/referral record ID |
| `contactId` | UUID | Parent contact ID |
| `referredToSocialWorker` | Boolean or null | Whether the contact resulted in social worker referral |
| `referredToSusalud` | Boolean or null | Whether the case was referred to SUSALUD |
| `susaludRegistrationNumber` | String or null | SUSALUD registration/tracking number |
| `receivedFoodGuide` | Boolean or null | Whether a food guide was provided |
| `participatesInGam` | Boolean or null | Whether the patient participates in GAM |
| `programSatisfaction` | String or null | Free-text satisfaction feedback |
| `wellbeingChanges` | String or null | Free-text perceived wellbeing changes |
| `knowsAboutFissal` | Boolean or null | Whether the patient/family knows about FISSAL |
| `referredToPaus` | Boolean or null | Whether the case was referred to PAUS |
| `referredToDae` | Boolean or null | Whether the case was referred to DAE |
| `referredToFissal` | Boolean or null | Whether the case was referred directly to FISSAL |
| `createdAt` | LocalDateTime | Timestamp |
| `updatedAt` | LocalDateTime | Last updated |

#### ContactSummary (embedded in history records)

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Contact ID |
| `agentName` | String or null | Agent's full name |
| `type` | ContactType | Type of contact |
| `status` | ContactStatus | Contact status |
| `purpose` | ContactPurpose | Purpose of contact |

---

### 5. Contacts

All endpoints under `/api/contacts`.

#### POST `/api/contacts` — ADMIN

Create a new contact record.

The optional `serviceReferral` object captures service delivery and derivation data linked 1:1 to the contact.

**Request body:**
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "agentId": "550e8400-e29b-41d4-a716-446655440001",
  "type": "CALL",
  "status": "SCHEDULED",
  "purpose": "FOLLOW_UP",
  "scheduledAt": "2025-06-10T14:00:00",
  "completedAt": null,
  "notes": "Seguimiento programado",
  "scheduledNextContactId": null,
  "serviceReferral": {
    "referredToSocialWorker": true,
    "referredToSusalud": false,
    "susaludRegistrationNumber": null,
    "receivedFoodGuide": true,
    "participatesInGam": true,
    "programSatisfaction": "Valora la orientacion brindada.",
    "wellbeingChanges": "Refiere sentirse mas acompanada.",
    "knowsAboutFissal": true,
    "referredToPaus": false,
    "referredToDae": false,
    "referredToFissal": false
  }
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440010",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "agentId": "550e8400-e29b-41d4-a716-446655440001",
  "type": "CALL",
  "status": "SCHEDULED",
  "purpose": "FOLLOW_UP",
  "scheduledAt": "2025-06-10T14:00:00",
  "completedAt": null,
  "notes": "Seguimiento programado",
  "scheduledNextContactId": null,
  "serviceReferral": {
    "id": "550e8400-e29b-41d4-a716-446655440011",
    "contactId": "550e8400-e29b-41d4-a716-446655440010",
    "referredToSocialWorker": true,
    "referredToSusalud": false,
    "susaludRegistrationNumber": null,
    "receivedFoodGuide": true,
    "participatesInGam": true,
    "programSatisfaction": "Valora la orientacion brindada.",
    "wellbeingChanges": "Refiere sentirse mas acompanada.",
    "knowsAboutFissal": true,
    "referredToPaus": false,
    "referredToDae": false,
    "referredToFissal": false,
    "createdAt": "2025-06-01T10:00:00",
    "updatedAt": "2025-06-01T10:00:00"
  },
  "createdAt": "2025-06-01T10:00:00",
  "updatedAt": "2025-06-01T10:00:00"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `404` — Patient/Agent not found

---

#### GET `/api/contacts` — JWT

List all contacts.

**Response `200 OK`:** List of ContactResponse.

**Status codes:** `200` — Success

---

#### GET `/api/contacts/{id}` — JWT

Get a single contact by UUID.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Single ContactResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### PUT `/api/contacts/{id}` — ADMIN

Update a contact. All fields optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "status": "COMPLETED",
  "completedAt": "2025-06-10T14:30:00",
  "notes": "Contacto completado exitosamente",
  "serviceReferral": {
    "referredToSusalud": true,
    "susaludRegistrationNumber": "SUS-2025-0001",
    "programSatisfaction": "Solicita seguimiento adicional.",
    "wellbeingChanges": "Aun presenta dudas sobre cobertura.",
    "referredToFissal": true
  }
}
```

**Response `200 OK`:** Updated ContactResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/api/contacts/{id}` — ADMIN

Delete a contact.

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

### 6. Volunteers

All endpoints under `/api/volunteers`.

#### POST `/api/volunteers` — ADMIN

Create a new volunteer.

**Request body:**
```json
{
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Ana",
  "lastName": "López",
  "specialty": "Psicooncología",
  "email": "ana.lopez@example.com",
  "phone": "+51999000777",
  "isActive": true
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440100",
  "userId": "550e8400-e29b-41d4-a716-446655440000",
  "firstName": "Ana",
  "lastName": "López",
  "specialty": "Psicooncología",
  "email": "ana.lopez@example.com",
  "phone": "+51999000777",
  "isActive": true,
  "createdAt": "2025-01-15T10:30:00",
  "updatedAt": "2025-01-15T10:30:00"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `404` — User not found

---

#### GET `/api/volunteers` — JWT

List all volunteers.

**Response `200 OK`:** List of VolunteerResponse.

**Status codes:** `200` — Success

---

#### GET `/api/volunteers/{id}` — JWT

Get a single volunteer by UUID.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Single VolunteerResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### PUT `/api/volunteers/{id}` — ADMIN

Update a volunteer. All fields optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "firstName": "Ana María",
  "lastName": "López García",
  "specialty": "Psicología Clínica",
  "email": "ana.lopez@updated.com",
  "phone": "+51999000888",
  "isActive": false
}
```

**Response `200 OK`:** Updated VolunteerResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/api/volunteers/{id}` — ADMIN

Delete a volunteer.

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

### 7. Volunteer Availability

All endpoints under `/api/volunteers/{volunteerId}/availability`.

#### POST `/api/volunteers/{volunteerId}/availability` — ADMIN

Create a new availability slot for a volunteer.

> **Note:** The frontend is expected to split date ranges into individual 1-hour slots before sending.

**Path parameters:** `volunteerId` — UUID

**Request body:**
```json
{
  "volunteerId": "550e8400-e29b-41d4-a716-446655440100",
  "date": "2025-06-15",
  "startTime": "09:00:00",
  "endTime": "10:00:00"
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440200",
  "volunteerId": "550e8400-e29b-41d4-a716-446655440100",
  "date": "2025-06-15",
  "startTime": "09:00:00",
  "endTime": "10:00:00",
  "status": "AVAILABLE"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `404` — Volunteer not found

---

#### GET `/api/volunteers/{volunteerId}/availability` — JWT

List availability slots for a volunteer. Supports optional date range filtering.

**Path parameters:** `volunteerId` — UUID

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `startDate` | LocalDate (`yyyy-MM-dd`) | No | Filter slots starting from this date |
| `endDate` | LocalDate (`yyyy-MM-dd`) | No | Filter slots up to this date |

> Both `startDate` and `endDate` must be provided together when filtering by date range.

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440200",
    "volunteerId": "550e8400-e29b-41d4-a716-446655440100",
    "date": "2025-06-15",
    "startTime": "09:00:00",
    "endTime": "10:00:00",
    "status": "AVAILABLE"
  }
]
```

**Status codes:** `200` — Success

---

#### GET `/api/volunteers/{volunteerId}/availability/{id}` — JWT

Get a single availability slot by UUID.

**Path parameters:**
- `volunteerId` — UUID
- `id` — UUID (slot ID)

**Response `200 OK`:** Single AvailabilitySlotResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### PUT `/api/volunteers/{volunteerId}/availability/{id}` — ADMIN

Update an availability slot. All fields optional.

**Path parameters:**
- `volunteerId` — UUID
- `id` — UUID (slot ID)

**Request body:**
```json
{
  "date": "2025-06-16",
  "startTime": "10:00:00",
  "endTime": "11:00:00",
  "status": "RESERVED"
}
```

**Response `200 OK`:** Updated AvailabilitySlotResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/api/volunteers/{volunteerId}/availability/{id}` — ADMIN

Delete an availability slot.

**Path parameters:**
- `volunteerId` — UUID
- `id` — UUID (slot ID)

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

### 8. Psychooncology Appointments

All endpoints under `/api/psychooncology-appointments`.

#### GET `/api/psychooncology-appointments` — JWT

List appointments with optional filters.

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `patientId` | UUID | No | Filter by patient |
| `volunteerId` | UUID | No | Filter by volunteer |
| `upcoming` | Boolean | No | `true` returns only upcoming appointments |

> Only one filter can be used at a time. If none provided, returns all appointments.

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440300",
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "volunteerId": "550e8400-e29b-41d4-a716-446655440100",
    "contactId": "550e8400-e29b-41d4-a716-446655440010",
    "availabilityId": "550e8400-e29b-41d4-a716-446655440200",
    "patientEmail": "maria.garcia@example.com",
    "sessionNumber": 1,
    "isAdditionalSession": false,
    "modality": "VIDEO_CALL",
    "status": "SCHEDULED",
    "scheduledAt": "2025-06-15T09:00:00",
    "completedAt": null,
    "topicAddressed": null,
    "sessionDetails": null,
    "additionalObservations": null,
    "recommendations": null,
    "referral": null,
    "createdAt": "2025-06-01T10:00:00",
    "updatedAt": "2025-06-01T10:00:00"
  }
]
```

**Status codes:** `200` — Success

---

#### GET `/api/psychooncology-appointments/{id}` — JWT

Get a single appointment by UUID.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Single PsychooncologyAppointmentResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### POST `/api/psychooncology-appointments` — ADMIN

Schedule a new psychooncology appointment. This changes the linked availability slot status to `RESERVED`.

**Request body:**
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "volunteerId": "550e8400-e29b-41d4-a716-446655440100",
  "contactId": "550e8400-e29b-41d4-a716-446655440010",
  "availabilityId": "550e8400-e29b-41d4-a716-446655440200",
  "patientEmail": "maria.garcia@example.com",
  "sessionNumber": 1,
  "isAdditionalSession": false,
  "modality": "VIDEO_CALL",
  "scheduledAt": "2025-06-15T09:00:00"
}
```

**Response `201 Created`:** PsychooncologyAppointmentResponse with status `SCHEDULED`.

**Status codes:** `201` — Created | `400` — Validation error | `404` — Referenced entity not found | `409` — Slot already reserved

---

#### PUT `/api/psychooncology-appointments/{id}` — ADMIN

Update an appointment. All fields optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "patientEmail": "maria.nueva@example.com",
  "sessionNumber": 2,
  "modality": "CALL",
  "status": "SCHEDULED"
}
```

**Response `200 OK`:** Updated PsychooncologyAppointmentResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/api/psychooncology-appointments/{id}` — ADMIN

Delete an appointment.

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

#### POST `/api/psychooncology-appointments/{id}/complete` — JWT

Complete an appointment with post-session data filled by the volunteer. Changes status to `COMPLETED`.

**Path parameters:** `id` — UUID

**Request body:** All fields optional; at least one may be provided.
```json
{
  "topicAddressed": "Ansiedad por tratamiento oncológico",
  "sessionDetails": "Se trabajaron técnicas de respiración y manejo de ansiedad",
  "additionalObservations": "Paciente receptiva, se recomienda continuar",
  "recommendations": "Ejercicios de respiración diarios",
  "referral": "CONTINUE_PSYCHOLOGY"
}
```

**Response `200 OK`:** Updated PsychooncologyAppointmentResponse with status `COMPLETED`.

**Status codes:** `200` — Completed | `404` — Not found | `400` — Invalid state transition

---

#### POST `/api/psychooncology-appointments/{id}/cancel` — JWT

Cancel an appointment. Changes status to `CANCELLED` and frees the availability slot (sets it back to `AVAILABLE`).

**Path parameters:** `id` — UUID

**Response `200 OK`**

**Status codes:** `200` — Cancelled | `404` — Not found | `400` — Invalid state transition

---

### PsychooncologyAppointmentResponse — Field Reference

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Appointment ID |
| `patientId` | UUID | Patient ID |
| `volunteerId` | UUID | Volunteer (psychooncologist) ID |
| `contactId` | UUID | Contact ID that originated the referral |
| `availabilityId` | UUID | Linked availability slot ID |
| `patientEmail` | String or null | Patient's email for session |
| `sessionNumber` | Int | Session number |
| `isAdditionalSession` | Boolean | Whether it's an extra session |
| `modality` | AppointmentModality | `CALL` or `VIDEO_CALL` |
| `status` | AppointmentStatus | `SCHEDULED`, `COMPLETED`, `CANCELLED`, `NO_ANSWER` |
| `scheduledAt` | LocalDateTime | Scheduled date/time |
| `completedAt` | LocalDateTime or null | Completion date/time |
| `topicAddressed` | String or null | Topics discussed |
| `sessionDetails` | String or null | Session details |
| `additionalObservations` | String or null | Additional observations |
| `recommendations` | String or null | Recommendations |
| `referral` | ReferralType or null | Referral type after session |
| `createdAt` | LocalDateTime | Timestamp |
| `updatedAt` | LocalDateTime | Last updated |

---

### 9. Health Centers

All endpoints under `/api/health-centers`.

#### GET `/api/health-centers` — JWT

List all health centers, optionally filtered by department.

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `department` | PeruDepartment | No | Filter by department |

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440020",
    "name": "Hospital Nacional",
    "slug": "hospital-nacional",
    "department": "LIMA",
    "isActive": true,
    "createdAt": "2025-01-01T00:00:00",
    "updatedAt": "2025-01-01T00:00:00"
  }
]
```

**Status codes:** `200` — Success

---

#### GET `/api/health-centers/{id}` — JWT

Get a single health center by UUID.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Single HealthCenterResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### GET `/api/health-centers/slug/{slug}` — JWT

Get a single health center by URL slug.

**Path parameters:** `slug` — String (e.g., `hospital-nacional`)

**Response `200 OK`:** Single HealthCenterResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### POST `/api/health-centers` — ADMIN

Create a new health center. The slug is auto-generated from the name.

**Request body:**
```json
{
  "name": "Hospital Nacional",
  "department": "LIMA"
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440020",
  "name": "Hospital Nacional",
  "slug": "hospital-nacional",
  "department": "LIMA",
  "isActive": true,
  "createdAt": "2025-01-01T00:00:00",
  "updatedAt": "2025-01-01T00:00:00"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `409` — Name already exists

---

#### PUT `/api/health-centers/{id}` — ADMIN

Update a health center. All fields optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "name": "Hospital Nacional Actualizado",
  "department": "CALLAO"
}
```

**Response `200 OK`:** Updated HealthCenterResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/api/health-centers/{id}` — ADMIN

Deactivate a health center (sets `isActive` to `false`).

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deactivated | `404` — Not found

---

#### PATCH `/api/health-centers/{id}/reactivate` — ADMIN

Reactivate a deactivated health center (sets `isActive` to `true`).

**Path parameters:** `id` — UUID

**Response `200 OK`:** Reactivated HealthCenterResponse.

**Status codes:** `200` — Reactivated | `404` — Not found

---

### 10. Alerts

All endpoints under `/api/alerts`.

#### GET `/api/alerts` — JWT

List alerts with optional filters.

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `healthCenterId` | UUID | No | Filter by health center |
| `status` | String | No | `"ACTIVE"` returns only active (unresolved) alerts |
| `agentId` | UUID | No | Filter by creating agent |

> Only one filter can be used at a time. If none provided, returns all alerts.

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440400",
    "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
    "healthCenterName": "Hospital Nacional",
    "contactId": "550e8400-e29b-41d4-a716-446655440010",
    "createdByAgentId": "550e8400-e29b-41d4-a716-446655440001",
    "createdByAgentName": "Juan Pérez",
    "title": "Paciente necesita atención urgente",
    "description": "La paciente María García requiere cita oncológica prioritaria",
    "status": "ACTIVE",
    "resolvedAt": null,
    "resolvedByAgentId": null,
    "resolvedByAgentName": null,
    "createdAt": "2025-06-01T10:00:00",
    "updatedAt": "2025-06-01T10:00:00"
  }
]
```

**Status codes:** `200` — Success

---

#### GET `/api/alerts/{id}` — JWT

Get a single alert by UUID.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Single AlertResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### POST `/api/alerts` — ADMIN

Create a new alert.

**Request body:**
```json
{
  "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
  "contactId": "550e8400-e29b-41d4-a716-446655440010",
  "createdByAgentId": "550e8400-e29b-41d4-a716-446655440001",
  "title": "Paciente necesita atención urgente",
  "description": "La paciente María García requiere cita oncológica prioritaria"
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440400",
  "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
  "healthCenterName": "Hospital Nacional",
  "contactId": "550e8400-e29b-41d4-a716-446655440010",
  "createdByAgentId": "550e8400-e29b-41d4-a716-446655440001",
  "createdByAgentName": "Juan Pérez",
  "title": "Paciente necesita atención urgente",
  "description": "La paciente María García requiere cita oncológica prioritaria",
  "status": "ACTIVE",
  "resolvedAt": null,
  "resolvedByAgentId": null,
  "resolvedByAgentName": null,
  "createdAt": "2025-06-01T10:00:00",
  "updatedAt": "2025-06-01T10:00:00"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `404` — Referenced entity not found

---

#### PUT `/api/alerts/{id}` — ADMIN

Update an alert. All fields optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "title": "Título actualizado",
  "description": "Descripción actualizada",
  "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
  "contactId": "550e8400-e29b-41d4-a716-446655440010"
}
```

**Response `200 OK`:** Updated AlertResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/api/alerts/{id}` — ADMIN

Delete an alert.

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

#### POST `/api/alerts/{id}/resolve` — ADMIN

Resolve an active alert. Changes status to `RESOLVED` and records who resolved it.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "resolvedByAgentId": "550e8400-e29b-41d4-a716-446655440001"
}
```

**Response `200 OK`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440400",
  "healthCenterId": "550e8400-e29b-41d4-a716-446655440020",
  "healthCenterName": "Hospital Nacional",
  "contactId": "550e8400-e29b-41d4-a716-446655440010",
  "createdByAgentId": "550e8400-e29b-41d4-a716-446655440001",
  "createdByAgentName": "Juan Pérez",
  "title": "Paciente necesita atención urgente",
  "description": "La paciente María García requiere cita oncológica prioritaria",
  "status": "RESOLVED",
  "resolvedAt": "2025-06-02T15:00:00",
  "resolvedByAgentId": "550e8400-e29b-41d4-a716-446655440001",
  "resolvedByAgentName": "Juan Pérez",
  "createdAt": "2025-06-01T10:00:00",
  "updatedAt": "2025-06-02T15:00:00"
}
```

**Status codes:** `200` — Resolved | `404` — Not found | `400` — Already resolved

---

### 11. Recordatorios

All endpoints under `/api/recordatorios`.

#### GET `/api/recordatorios` — JWT

List all reminders for a given patient, ordered by scheduled date ascending.

**Query parameters:**
| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `patientId` | UUID | Yes | Filter by patient |

**Response `200 OK`:**
```json
[
  {
    "id": "550e8400-e29b-41d4-a716-446655440500",
    "patientId": "550e8400-e29b-41d4-a716-446655440000",
    "contactId": "550e8400-e29b-41d4-a716-446655440010",
    "type": "LABORATORIO",
    "description": "Hemograma completo",
    "scheduledDate": "2026-06-15",
    "status": "PENDIENTE",
    "notes": "Recordatorio para el paciente",
    "createdAt": "2025-06-10T10:00:00",
    "updatedAt": "2025-06-10T10:00:00"
  }
]
```

**Status codes:** `200` — Success

---

#### POST `/api/recordatorios` — ADMIN

Create a new reminder for a patient.

**Request body:**
```json
{
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "contactId": "550e8400-e29b-41d4-a716-446655440010",
  "type": "LABORATORIO",
  "description": "Hemograma completo",
  "scheduledDate": "2026-06-15",
  "notes": "Recordatorio para el paciente"
}
```

**Response `201 Created`:**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440500",
  "patientId": "550e8400-e29b-41d4-a716-446655440000",
  "contactId": "550e8400-e29b-41d4-a716-446655440010",
  "type": "LABORATORIO",
  "description": "Hemograma completo",
  "scheduledDate": "2026-06-15",
  "status": "PENDIENTE",
  "notes": "Recordatorio para el paciente",
  "createdAt": "2025-06-10T10:00:00",
  "updatedAt": "2025-06-10T10:00:00"
}
```

**Status codes:** `201` — Created | `400` — Validation error | `404` — Patient/Contact not found

---

#### PUT `/api/recordatorios/{id}` — ADMIN

Update a reminder. All fields optional.

**Path parameters:** `id` — UUID

**Request body:**
```json
{
  "type": "IMAGEN",
  "description": "Tomografía de tórax",
  "scheduledDate": "2026-06-20",
  "notes": "Actualizado"
}
```

**Response `200 OK`:** Updated ReminderResponse.

**Status codes:** `200` — Success | `404` — Not found

---

#### DELETE `/api/recordatorios/{id}` — ADMIN

Delete a reminder.

**Path parameters:** `id` — UUID

**Response `204 No Content`**

**Status codes:** `204` — Deleted | `404` — Not found

---

#### POST `/api/recordatorios/{id}/complete` — ADMIN

Mark a reminder as completed. Changes status to `COMPLETADO`.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Updated ReminderResponse with status `COMPLETADO`.

**Status codes:** `200` — Completed | `404` — Not found | `400` — Invalid state transition (already completed/canceled)

---

#### POST `/api/recordatorios/{id}/cancel` — ADMIN

Cancel a reminder. Changes status to `CANCELADO`.

**Path parameters:** `id` — UUID

**Response `200 OK`:** Updated ReminderResponse with status `CANCELADO`.

**Status codes:** `200` — Canceled | `404` — Not found | `400` — Invalid state transition (already completed/canceled)

---

### ReminderResponse — Field Reference

| Field | Type | Description |
|-------|------|-------------|
| `id` | UUID | Reminder ID |
| `patientId` | UUID | Patient ID |
| `contactId` | UUID | Contact ID that originated the reminder |
| `type` | ReminderType | Type of reminder |
| `description` | String | Description of the reminder |
| `scheduledDate` | LocalDate | Scheduled date |
| `status` | ReminderStatus | `PENDIENTE`, `COMPLETADO`, or `CANCELADO` |
| `notes` | String or null | Optional notes |
| `createdAt` | LocalDateTime | Timestamp |
| `updatedAt` | LocalDateTime | Last updated |

---

## Endpoint Summary

| # | Method | Path | Auth | Description |
|---|--------|------|------|-------------|
| | | **Auth** | | |
| 1 | POST | `/auth/login` | Public | Authenticate and get tokens |
| 2 | POST | `/auth/refresh` | Public | Refresh access token |
| | | **Users** | | |
| 3 | GET | `/users` | JWT | List users (paginated) |
| 4 | GET | `/users/{id}` | JWT | Get user by ID |
| 5 | POST | `/users` | ADMIN | Create user |
| 6 | PUT | `/users/{id}` | ADMIN | Update user |
| 7 | DELETE | `/users/{id}` | ADMIN | Delete user |
| | | **Agents** | | |
| 8 | POST | `/agents` | ADMIN | Create agent |
| 9 | GET | `/agents` | JWT | List agents |
| 10 | GET | `/agents/{id}` | JWT | Get agent by ID |
| 11 | PUT | `/agents/{id}` | ADMIN | Update agent |
| 12 | DELETE | `/agents/{id}` | ADMIN | Delete agent |
| | | **Patients** | | |
| 13 | GET | `/api/patients` | JWT | List all patients |
| 14 | GET | `/api/patients/status/{status}` | JWT | Filter patients by status |
| 15 | GET | `/api/patients/role/{role}` | JWT | Filter patients by role |
| 16 | GET | `/api/patients/{id}` | JWT | Get patient by ID (full detail) |
| 17 | POST | `/api/patients` | JWT | Create basic patient |
| 18 | PUT | `/api/patients/{id}` | JWT | Update patient basic info |
| 19 | PATCH | `/api/patients/{id}/status` | JWT | Change patient status |
| 20 | POST | `/api/patients/enroll` | JWT | Full enrollment (single transaction) |
| 21 | PUT | `/api/patients/{id}/details` | JWT | Update patient details |
| 22 | GET | `/api/patients/{id}/insurance` | JWT | Get insurance history |
| 23 | POST | `/api/patients/{id}/insurance` | JWT | Add insurance record |
| 24 | GET | `/api/patients/{id}/diagnoses` | JWT | Get diagnosis history |
| 25 | POST | `/api/patients/{id}/diagnoses` | JWT | Add diagnosis record |
| 26 | GET | `/api/patients/{id}/treatments` | JWT | Get treatment history |
| 27 | POST | `/api/patients/{id}/treatments` | JWT | Add treatment record |
| 28 | GET | `/api/patients/{id}/appointments` | JWT | Get medical appointment history |
| 29 | POST | `/api/patients/{id}/appointments` | JWT | Add medical appointment |
| 30 | GET | `/api/patients/{id}/sis` | JWT | Get SIS affiliation history |
| 31 | POST | `/api/patients/{id}/sis` | JWT | Add SIS affiliation record |
| 32 | PATCH | `/api/patients/{id}/sis/{sisId}/affiliate` | JWT | Mark SIS as affiliated |
| 34 | GET | `/api/patients/companion/{companionId}/patients` | JWT | Get patients by companion |
| 35 | GET | `/api/patients/{id}/companions` | JWT | Get patient's companions |
| 36 | POST | `/api/patients/{id}/companions` | JWT | Link companion to patient |
| 37 | DELETE | `/api/patients/{id}/companions/{companionId}` | JWT | Unlink companion |
| 38 | GET | `/api/patients/{id}/contacts` | JWT | Get patient's contact history |
| | | **Contacts** | | |
| 39 | POST | `/api/contacts` | ADMIN | Create contact |
| 40 | GET | `/api/contacts` | JWT | List contacts |
| 41 | GET | `/api/contacts/{id}` | JWT | Get contact by ID |
| 42 | PUT | `/api/contacts/{id}` | ADMIN | Update contact |
| 43 | DELETE | `/api/contacts/{id}` | ADMIN | Delete contact |
| | | **Volunteers** | | |
| 44 | POST | `/api/volunteers` | ADMIN | Create volunteer |
| 45 | GET | `/api/volunteers` | JWT | List volunteers |
| 46 | GET | `/api/volunteers/{id}` | JWT | Get volunteer by ID |
| 47 | PUT | `/api/volunteers/{id}` | ADMIN | Update volunteer |
| 48 | DELETE | `/api/volunteers/{id}` | ADMIN | Delete volunteer |
| | | **Volunteer Availability** | | |
| 49 | POST | `/api/volunteers/{volunteerId}/availability` | ADMIN | Create availability slot |
| 50 | GET | `/api/volunteers/{volunteerId}/availability` | JWT | List availability (with optional date filter) |
| 51 | GET | `/api/volunteers/{volunteerId}/availability/{id}` | JWT | Get availability slot |
| 52 | PUT | `/api/volunteers/{volunteerId}/availability/{id}` | ADMIN | Update availability slot |
| 53 | DELETE | `/api/volunteers/{volunteerId}/availability/{id}` | ADMIN | Delete availability slot |
| | | **Psychooncology Appointments** | | |
| 54 | GET | `/api/psychooncology-appointments` | JWT | List appointments (with optional filters) |
| 55 | GET | `/api/psychooncology-appointments/{id}` | JWT | Get appointment by ID |
| 56 | POST | `/api/psychooncology-appointments` | ADMIN | Schedule appointment |
| 57 | PUT | `/api/psychooncology-appointments/{id}` | ADMIN | Update appointment |
| 58 | DELETE | `/api/psychooncology-appointments/{id}` | ADMIN | Delete appointment |
| 59 | POST | `/api/psychooncology-appointments/{id}/complete` | JWT | Complete appointment |
| 60 | POST | `/api/psychooncology-appointments/{id}/cancel` | JWT | Cancel appointment |
| | | **Health Centers** | | |
| 61 | GET | `/api/health-centers` | JWT | List health centers (with optional department filter) |
| 62 | GET | `/api/health-centers/{id}` | JWT | Get health center by ID |
| 63 | GET | `/api/health-centers/slug/{slug}` | JWT | Get health center by slug |
| 64 | POST | `/api/health-centers` | ADMIN | Create health center |
| 65 | PUT | `/api/health-centers/{id}` | ADMIN | Update health center |
| 66 | DELETE | `/api/health-centers/{id}` | ADMIN | Deactivate health center |
| 67 | PATCH | `/api/health-centers/{id}/reactivate` | ADMIN | Reactivate health center |
| | | **Alerts** | | |
| 68 | GET | `/api/alerts` | JWT | List alerts (with optional filters) |
| 69 | GET | `/api/alerts/{id}` | JWT | Get alert by ID |
| 70 | POST | `/api/alerts` | ADMIN | Create alert |
| 71 | PUT | `/api/alerts/{id}` | ADMIN | Update alert |
| 72 | DELETE | `/api/alerts/{id}` | ADMIN | Delete alert |
| 73 | POST | `/api/alerts/{id}/resolve` | ADMIN | Resolve alert |
| | | **Recordatorios** | | |
| 74 | GET | `/api/recordatorios` | JWT | List reminders by patient |
| 75 | POST | `/api/recordatorios` | ADMIN | Create reminder |
| 76 | PUT | `/api/recordatorios/{id}` | ADMIN | Update reminder |
| 77 | DELETE | `/api/recordatorios/{id}` | ADMIN | Delete reminder |
| 78 | POST | `/api/recordatorios/{id}/complete` | ADMIN | Complete reminder |
| 79 | POST | `/api/recordatorios/{id}/cancel` | ADMIN | Cancel reminder |

---

*Generated from source code — all controllers, DTOs, and enums read from the fpc-back project.*
