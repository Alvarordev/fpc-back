# User & Authentication Specification

## Purpose
Define user management and JWT authentication for the FPC backend.

## Functional Requirements

### Requirement: User Data Model
The system MUST store users with UUID PK, unique email, BCrypt password hash, role (ADMIN, AGENT, PSYCHOLOGIST), is_active, created_at, and updated_at.

#### Scenario: Admin seed
- GIVEN an empty database
- WHEN Flyway runs V2
- THEN an admin user exists with email `admin@gmail.com`

### Requirement: Authentication
The system MUST authenticate by email/password and return JWT access (~15 min) and refresh (~7 days) tokens.

#### Scenario: Login success
- GIVEN a registered active user
- WHEN `POST /auth/login` with valid credentials
- THEN response contains access and refresh tokens

#### Scenario: Login failure
- GIVEN a registered user
- WHEN `POST /auth/login` with invalid password
- THEN response is HTTP 401

### Requirement: Token Refresh
The system MUST issue a new access token given a valid refresh token.

#### Scenario: Refresh success
- GIVEN a valid refresh token
- WHEN `POST /auth/refresh`
- THEN response contains new access token

### Requirement: User CRUD
The system MUST provide CRUD at `/users` protected by JWT. Only ADMIN SHALL create or delete users.

#### Scenario: Admin creates user
- GIVEN an authenticated ADMIN
- WHEN `POST /users` with valid data
- THEN user is created with HTTP 201

#### Scenario: Agent creates user
- GIVEN an authenticated AGENT
- WHEN `POST /users`
- THEN response is HTTP 403

#### Scenario: List users
- GIVEN an authenticated user
- WHEN `GET /users`
- THEN paginated user list is returned

## Non-Functional Requirements

| ID | Requirement |
|---|---|
| NFR1 | Passwords SHALL be hashed with BCrypt. |
| NFR2 | All `/users/**` endpoints MUST reject unauthenticated requests with HTTP 401. |
| NFR3 | Token secrets SHOULD be externalized via environment variables. |
| NFR4 | Auth endpoints SHOULD respond within 500ms under normal load. |

## Data Requirements

| Field | Type | Constraints |
|---|---|---|
| id | UUID | PK, auto-generated |
| email | String | Unique, not null |
| password_hash | String | Not null |
| role | Enum | ADMIN, AGENT, PSYCHOLOGIST |
| is_active | Boolean | Default true |
| created_at | Timestamp | Auto-generated |
| updated_at | Timestamp | Auto-updated |

## API Specifications

| Endpoint | Method | Auth | Request Body | Response | Status |
|---|---|---|---|---|---|
| /auth/login | POST | Public | `{email, password}` | `{accessToken, refreshToken, tokenType}` | 200, 401 |
| /auth/refresh | POST | Public | `{refreshToken}` | `{accessToken, refreshToken, tokenType}` | 200, 401 |
| /users | GET | Bearer | Query page/size | Page&lt;UserDTO&gt; | 200, 401 |
| /users | POST | Bearer (ADMIN) | `{email, password, role}` | UserDTO | 201, 403 |
| /users/{id} | GET | Bearer | Path id | UserDTO | 200, 404 |
| /users/{id} | PUT | Bearer | Path id, body | UserDTO | 200, 404 |
| /users/{id} | DELETE | Bearer (ADMIN) | Path id | Empty | 204, 403 |

## Security Specifications

- Tokens MUST be sent in `Authorization: Bearer <token>`.
- Access tokens MUST expire after ~15 minutes.
- Refresh tokens MUST expire after ~7 days.
- The system MUST validate signature and expiration on every protected request.
- Role checks MUST enforce ADMIN for POST/DELETE on `/users`.

## Error Handling Specifications

| Condition | Status | Body |
|---|---|---|
| Invalid credentials | 401 | `{error, message}` |
| Missing/invalid token | 401 | `{error, message}` |
| Forbidden role | 403 | `{error, message}` |
| User not found | 404 | `{error, message}` |
| Validation failure | 400 | `{error, details}` |

## Test Requirements

- Unit: BCrypt hashing, JWT generation/validation.
- Integration: Login/refresh, endpoint authorization, CRUD with roles.
- Contract: Request/response shapes and status codes.
