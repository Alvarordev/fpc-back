# Proposal: Create Users & Authentication System

## Intent

Establish a secure user management and authentication foundation for the FPC backend. This enables role-based access control (ADMIN, AGENT, PSYCHOLOGIST) and protects all business endpoints with JWT Bearer tokens.

## Scope

### In Scope
- `users` table migration with UUID primary keys and admin seed
- JPA `User` entity and `UserRole` enum
- `UserRepository` with Spring Data JPA
- Auth service: login + refresh returning JWT access/refresh tokens
- Spring Security 6.x with JWT filter chain
- Full CRUD REST API at `/users` protected by JWT
- `BCryptPasswordEncoder` for password hashing

### Out of Scope
- OAuth2 / social login
- Email verification or password reset
- Refresh token revocation / blacklisting
- Frontend integration or role-based UI rendering

## Approach

Adopt **Package-by-Feature (Layered)** inside `com.hazardev.fpc_back`:

```
user/
  domain/         → User.kt, UserRole.kt
  infrastructure/ → UserRepository.kt
  application/    → UserService.kt, AuthService.kt, DTOs
  api/            → UserController.kt, AuthController.kt
shared/
  config/         → SecurityConfig.kt, JwtProperties.kt
  security/       → JwtTokenProvider.kt, JwtAuthenticationFilter.kt, CustomUserDetailsService.kt
exception/        → GlobalExceptionHandler.kt
```

**Security**: Stateless sessions, `SecurityFilterChain` permitting `/auth/**`, custom `OncePerRequestFilter` validating `Authorization: Bearer <token>`, `@PreAuthorize` for admin endpoints.

**JWT**: Access token (15 min), refresh token (7 days) via `io.jsonwebtoken:jjwt`. No persistent refresh store for MVP.

**Migration**: `V2__create_users_table.sql` with `gen_random_uuid()` PK, unique email, and hashed admin seed (`admin@gmail.com` / `123456`).

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `build.gradle.kts` | Modified | Add `spring-boot-starter-security`, `jjwt-*`, `spring-boot-starter-test` |
| `application.properties` | Modified | JWT secret, expiration, security settings |
| `db/migration/V2__create_users_table.sql` | New | Users table + admin seed |
| `src/main/kotlin/...` | New | Domain, infra, app, API, config, security packages |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Seed password in VCS | High | Document as dev-only; force change on production deploy |
| JWT secret hardcoded | Med | Externalize via env var in `application.properties` |
| No refresh revocation | Med | Document as MVP limitation; add blacklist table later |
| Hibernate validate failure | Low | Ensure Flyway migration runs before app startup |
| Nested project directory | Low | Verify all file paths target `fpc-back/fpc-back/src/...` |

## Rollback Plan

1. Revert `build.gradle.kts` dependency additions.
2. Delete new source directories.
3. Revert `application.properties` changes.
4. In development, use Flyway `clean` or `repair` to remove V2 migration. In production, create a compensating `V3` migration to drop the table if absolutely necessary.

## Dependencies

- `spring-boot-starter-security`
- `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson`
- `spring-boot-starter-test`

## Success Criteria

- [ ] `POST /auth/login` returns access and refresh tokens for valid credentials
- [ ] `POST /auth/refresh` returns new token pair given a valid refresh token
- [ ] `GET /users` and `GET /users/{id}` reject unauthenticated requests
- [ ] `POST /users` requires ADMIN role
- [ ] Admin user exists with seeded credentials
- [ ] All passwords stored as BCrypt hashes
