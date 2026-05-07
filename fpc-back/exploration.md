# Exploration: Users & Authentication System

## Current State

The project `fpc-back` is a minimal Spring Boot 4.0.6 + Kotlin 2.2.21 application with virtually no domain code. The codebase consists of:
- **Main class**: `FpcBackApplication.kt` — bare `@SpringBootApplication`
- **Test**: `FpcBackApplicationTests.kt` — only a context-load smoke test
- **Database**: PostgreSQL configured at `localhost:5432/fpc_db` with Flyway enabled
- **JPA**: Hibernate in `validate` mode with PostgreSQL dialect
- **Migration**: `V1__init.sql` exists but is empty (baseline only)
- **No existing entities, repositories, services, controllers, or security configuration**

**Working directory note**: The actual Gradle project is nested at `C:\Users\Hazard\Code\fpc-back\fpc-back\` (double directory). All new code should be created inside this subdirectory.

### Existing Dependencies (build.gradle.kts)
| Dependency | Status |
|------------|--------|
| `spring-boot-starter-data-jpa` | Present |
| `spring-boot-starter-flyway` | Present |
| `spring-boot-starter-validation` | Present |
| `spring-boot-starter-webmvc` | Present |
| `spring-boot-starter-websocket` | Present |
| `jackson-module-kotlin` | Present |
| `postgresql` | Present (runtime) |
| `kotlin-jpa` plugin | Present (handles `open` entities) |

### Missing Dependencies
| Dependency | Needed For |
|------------|------------|
| `spring-boot-starter-security` | Spring Security filter chain, PasswordEncoder |
| `spring-boot-starter-test` | `@SpringBootTest`, MockMvc, `@WebMvcTest` (note: current test deps are `data-jpa-test`, `flyway-test`, etc., but not the main starter-test) |
| JWT library (e.g., `io.jsonwebtoken:jjwt`) | JWT token generation and validation |
| `kotlin-test-junit5` | Present, sufficient for unit tests |

---

## Affected Areas
- `fpc-back/build.gradle.kts` — must add Spring Security + JWT dependencies
- `fpc-back/src/main/resources/application.properties` — must add JWT secret, expiration times, and security settings
- `fpc-back/src/main/resources/db/migration/` — new Flyway migration `V2__create_users_table.sql`
- `fpc-back/src/main/kotlin/com/hazardev/fpc_back/` — entire package tree needs creation (no domain layer exists)

---

## Approaches

### 1. Package-by-Feature (Layered)
Organize code around the `user` feature domain.
```
com.hazardev.fpc_back
├── user/
│   ├── domain/
│   │   ├── User.kt (JPA Entity)
│   │   └── UserRole.kt (Enum)
│   ├── infrastructure/
│   │   └── UserRepository.kt (Spring Data JPA)
│   ├── application/
│   │   ├── UserService.kt
│   │   ├── AuthService.kt
│   │   └── dto/
│   │       ├── CreateUserRequest.kt
│   │       ├── UpdateUserRequest.kt
│   │       ├── UserResponse.kt
│   │       ├── LoginRequest.kt
│   │       ├── TokenResponse.kt
│   │       └── RefreshTokenRequest.kt
│   └── api/
│       ├── UserController.kt (@RestController /users)
│       └── AuthController.kt (@RestController /auth)
├── shared/
│   ├── config/
│   │   ├── SecurityConfig.kt (filter chain, password encoder)
│   │   └── JwtProperties.kt (@ConfigurationProperties)
│   └── security/
│       ├── JwtTokenProvider.kt (generate/validate JWT)
│       ├── JwtAuthenticationFilter.kt (OncePerRequestFilter)
│       └── CustomUserDetailsService.kt (UserDetailsService impl)
└── exception/
    └── GlobalExceptionHandler.kt (@ControllerAdvice)
```
**Pros**: Clean separation, easy to navigate, aligns with DDD-lite; each feature is self-contained.
**Cons**: Slightly more packages than a flat structure.
**Effort**: Medium

### 2. Flat Layered (Controller/Service/Repository)
```
com.hazardev.fpc_back
├── controller/
├── service/
├── repository/
├── model/
├── dto/
├── config/
└── security/
```
**Pros**: Simple, familiar to Java developers.
**Cons**: Scales poorly; unrelated features clutter the same package; not idiomatic for modern Kotlin/Spring Boot.
**Effort**: Low

### 3. Hexagonal / Clean Architecture
Introduce ports & adapters, domain services, application services, and infrastructure.
**Pros**: Maximum testability, strict dependency rules, future-proof.
**Cons**: Overkill for a minimal app with a single feature; steep overhead for a small team.
**Effort**: High

---

## Recommendation

**Adopt Approach 1: Package-by-Feature (Layered)** with the directory structure shown above. It strikes the best balance between clean architecture and pragmatic Spring Boot Kotlin development. It also aligns with the `kotlin-springboot` skill recommendation to organize by feature/domain rather than by layer.

### Security Approach
1. **Spring Security 6.x** (pulled in via `spring-boot-starter-security`) with a `SecurityFilterChain` bean.
2. **JWT via JJWT** (`io.jsonwebtoken:jjwt-api/impl/jackson`) — most popular and well-documented library for Spring Boot 3+.
3. **Stateless session** (`SessionCreationPolicy.STATELESS`) since we are token-based.
4. **Password hashing** with `BCryptPasswordEncoder`.
5. **Filter-based JWT validation** — a custom `OncePerRequestFilter` that reads the `Authorization: Bearer <token>` header, validates it, and sets the `SecurityContextHolder`.
6. **Role-based access control** — `@PreAuthorize("hasRole('ADMIN')")` on admin-specific endpoints. The requirement says "all routes protected by JWT except login/refresh," so the base configuration permits `/auth/**` and secures everything else.

### JWT Token Strategy
- **Access Token**: Short-lived (e.g., 15 minutes), contains `sub` (userId), `email`, `role`.
- **Refresh Token**: Longer-lived (e.g., 7 days), stored only in memory/client. A simple `refreshToken` endpoint accepts a valid refresh token and returns a new access + refresh token pair.
- **No refresh token persistence**: For this MVP, we do not store refresh tokens in the DB. If revocation is needed later, a `refresh_tokens` table can be added.

### API Design
| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| POST | `/auth/login` | Public | Email + password → access + refresh tokens |
| POST | `/auth/refresh` | Public (valid refresh token) | New token pair |
| GET | `/users` | JWT | List all users |
| GET | `/users/{id}` | JWT | Get user by ID |
| POST | `/users` | JWT (ADMIN) | Create user |
| PUT | `/users/{id}` | JWT (ADMIN or self) | Update user |
| DELETE | `/users/{id}` | JWT (ADMIN) | Delete user |

### Migration Strategy
- Create `V2__create_users_table.sql` with `UUID` primary key (using `gen_random_uuid()` for PostgreSQL 13+), `email` (unique), `password_hash`, `role` (varchar/enum), `is_active`, `created_at`, `updated_at`.
- Seed the admin user in the same migration: `admin@gmail.com` / hashed `123456`.

### Risks
1. **Password in migration**: The seed admin password (`123456`) will be visible in the migration file and version control. This is acceptable for a local/dev seed, but MUST be changed and documented for production.
2. **JWT secret management**: `application.properties` will need a JWT secret. Hardcoding it in properties is a risk for production; should be externalized via environment variables.
3. **No refresh token blacklisting**: Without a persistent refresh token store, stolen refresh tokens cannot be revoked. This is acceptable for MVP but should be documented.
4. **Spring Boot 4.x version**: Spring Boot `4.0.6` is not a standard release as of common knowledge. If this is a typo for `3.2.x` or `3.3.x`, the security configuration (especially `SecurityFilterChain` DSL) remains the same. If it truly is a future/major version, minor API adjustments may be needed.
5. **Nested project directory**: The actual project lives inside `fpc-back/fpc-back/`. Care must be taken to create files in the correct location (`C:\Users\Hazard\Code\fpc-back\fpc-back\src\...`).
6. **Hibernate `validate` mode**: Flyway must create the `users` table BEFORE the app starts, otherwise Hibernate will fail on startup.

### Ready for Proposal
**Yes.** The exploration confirms a greenfield implementation within an existing minimal Spring Boot app. The next step is to create a formal change proposal (`sdd-propose`) that documents the scope, approach, and dependencies.
