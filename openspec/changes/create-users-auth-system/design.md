# Design: Create Users & Authentication System

## Technical Approach

Implement a stateless JWT-backed auth layer and full user CRUD using Package-by-Feature (Layered) under `com.hazardev.fpc_back.user`. Spring Security 6.x filters validate Bearer tokens on every request except `/auth/**`. BCrypt hashes passwords; JJWT generates and parses access/refresh tokens. Flyway manages the `users` table schema and admin seed.

## Architecture Decisions

| Decision | Choice | Alternatives | Rationale |
|----------|--------|--------------|-----------|
| Package structure | Package-by-Feature (domain / infra / app / api) | Package-by-layer | Keeps user-related code cohesive; aligns with proposal |
| JWT library | JJWT 0.12.6 | Java-JWT, Auth0 | Native Kotlin-friendly API, strong Spring Boot 4.x compatibility |
| Token storage | Stateless (no DB table) | Persistent refresh store | MVP scope; add revocation later if needed |
| Password encoder | BCryptPasswordEncoder (strength 10) | PBKDF2, Argon2 | Spring Security default, well-audited, sufficient for MVP |
| Role enforcement | `@PreAuthorize` on controllers | Manual checks in service | Declarative, testable, leverages Spring Security expression language |
| ID type | UUID (`gen_random_uuid()`) | Auto-increment Long | Secure, non-sequential, fits multi-service future |
| DTO mapping | Manual constructors / extension functions | MapStruct | Minimal boilerplate in Kotlin; avoids extra dependency for MVP |

## Data Flow

```
Login Flow
----------
Client → AuthController → AuthService → UserRepository → PostgreSQL
              ↓                ↓
         BCrypt matches    JwtTokenProvider
              ↓                ↓
         TokenResponse ←── access + refresh JWTs

Request Flow
------------
Client → JwtAuthenticationFilter → SecurityContextHolder → Controller → Service → Repository
              ↑
        JwtTokenProvider validates Bearer token
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `build.gradle.kts` | Modify | Add `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` |
| `application.properties` | Modify | JWT secret, access/refresh expiration |
| `db/migration/V2__create_users_table.sql` | Create | Users table + admin seed |
| `user/domain/User.kt` | Create | JPA entity with UUID PK, email, passwordHash, role, isActive, timestamps |
| `user/domain/UserRole.kt` | Create | Enum: `ADMIN`, `AGENT`, `PSYCHOLOGIST` |
| `user/infrastructure/UserRepository.kt` | Create | Spring Data JPA interface |
| `user/application/dto/*.kt` | Create | `LoginRequest`, `TokenResponse`, `CreateUserRequest`, `UpdateUserRequest`, `UserResponse` |
| `user/application/AuthService.kt` | Create | Login validation, token generation |
| `user/application/UserService.kt` | Create | CRUD business logic |
| `user/api/AuthController.kt` | Create | `POST /auth/login`, `POST /auth/refresh` |
| `user/api/UserController.kt` | Create | `GET/POST/PUT/DELETE /users` |
| `shared/config/SecurityConfig.kt` | Create | Filter chain, password encoder bean, method security |
| `shared/config/JwtProperties.kt` | Create | `@ConfigurationProperties` for JWT settings |
| `shared/security/JwtTokenProvider.kt` | Create | Generate / parse / validate JWTs |
| `shared/security/JwtAuthenticationFilter.kt` | Create | `OncePerRequestFilter` extracting Bearer tokens |
| `shared/security/CustomUserDetailsService.kt` | Create | Load user by email for Spring Security |
| `shared/exception/GlobalExceptionHandler.kt` | Create | `@ControllerAdvice` returning structured error responses |

## Interfaces / Contracts

### Auth API

| Endpoint | Method | Auth | Body | Response |
|----------|--------|------|------|----------|
| `/auth/login` | POST | Public | `LoginRequest` | `TokenResponse` |
| `/auth/refresh` | POST | Public | `RefreshRequest` | `TokenResponse` |

### Users API

| Endpoint | Method | Auth | Role | Body | Response |
|----------|--------|------|------|------|----------|
| `/users` | GET | Bearer | Any | — | `List<UserResponse>` |
| `/users/{id}` | GET | Bearer | Any | — | `UserResponse` |
| `/users` | POST | Bearer | ADMIN | `CreateUserRequest` | `UserResponse` |
| `/users/{id}` | PUT | Bearer | ADMIN | `UpdateUserRequest` | `UserResponse` |
| `/users/{id}` | DELETE | Bearer | ADMIN | — | `204 No Content` |

### DTOs (Kotlin data classes)

```kotlin
data class LoginRequest(val email: String, val password: String)
data class TokenResponse(val accessToken: String, val refreshToken: String)
data class CreateUserRequest(val email: String, val password: String, val role: UserRole)
data class UpdateUserRequest(val email: String?, val password: String?, val role: UserRole?, val isActive: Boolean?)
data class UserResponse(val id: UUID, val email: String, val role: UserRole, val isActive: Boolean, val createdAt: Instant, val updatedAt: Instant)
```

## Database Schema

```sql
CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'AGENT', 'PSYCHOLOGIST')),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);
```

Admin seed inserted in the same migration with pre-hashed BCrypt password.

## Security Architecture

- **Filter chain**: `SecurityFilterChain` permits `/auth/**`, requires authentication for all other routes. Adds `JwtAuthenticationFilter` before `UsernamePasswordAuthenticationFilter`.
- **Password encoding**: `BCryptPasswordEncoder` bean; raw passwords never persisted.
- **JWT validation**: `JwtAuthenticationFilter` extracts `Authorization: Bearer <token>`, calls `JwtTokenProvider.validateToken()` and `getAuthentication()`, sets `SecurityContextHolder`.
- **Role checks**: `@EnableMethodSecurity` + `@PreAuthorize("hasRole('ADMIN')")` on admin-only endpoints.
- **Token strategy**: Access token (15 min), refresh token (7 days). Claims include `sub` (user ID), `email`, `role`, `type` (`access`/`refresh`).

## Error Handling Strategy

- `GlobalExceptionHandler` catches:
  - `AuthenticationException` → `401 Unauthorized`
  - `AccessDeniedException` → `403 Forbidden`
  - `EntityNotFoundException` / `UsernameNotFoundException` → `404 Not Found`
  - `IllegalArgumentException` / validation errors → `400 Bad Request`
  - `Exception` → `500 Internal Server Error`
- Standard error body: `ErrorResponse(timestamp, status, error, message, path)`.

## Dependencies

```kotlin
implementation("org.springframework.boot:spring-boot-starter-security")
implementation("io.jsonwebtoken:jjwt-api:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
```

Versions align with Spring Boot 4.0.6 BOM where applicable; JJWT is explicit because it is not in the Spring BOM.

## Configuration Properties

```properties
# JWT
jwt.secret=${JWT_SECRET:change-me-in-production}
jwt.access-expiration=900000
jwt.refresh-expiration=604800000
```

Externalize `jwt.secret` via environment variable in production.

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| Unit | `AuthService`, `UserService`, `JwtTokenProvider` | JUnit 5 + Mockito; mock repository and encoder |
| Integration | Auth and User controllers, security filter chain | `@SpringBootTest(webEnvironment = RANDOM_PORT)` + `TestRestTemplate` |
| Security | Role-based access on endpoints | `@WithMockUser` for unit-like controller tests; full JWT integration tests for filter chain |
| Migration | Flyway V2 executes cleanly | Spring Boot test context with `ddl-auto=validate` |

## Migration / Rollback

- Migration `V2__create_users_table.sql` is idempotent in creation; admin seed uses `INSERT ... ON CONFLICT DO NOTHING` if re-run.
- Rollback: revert source deletions and drop the `users` table via a compensating migration only if required in production.

## Open Questions

- None.
