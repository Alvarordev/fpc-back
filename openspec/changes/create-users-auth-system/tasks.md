# Tasks: Create Users & Authentication System

## Phase 1: Dependencies & Configuration

- [x] 1.1 Add `spring-boot-starter-security`, `jjwt-api`, `jjwt-impl`, `jjwt-jackson` to `fpc-back/build.gradle.kts`
- [x] 1.2 Add `jwt.secret`, `jwt.access-expiration`, `jwt.refresh-expiration` to `fpc-back/src/main/resources/application.properties`
- [x] 1.3 Create `fpc-back/src/main/resources/db/migration/V2__create_users_table.sql` with users table + admin seed

**Verification**: `./gradlew dependencies --configuration runtimeClasspath` shows jjwt jars; app starts without Flyway errors

## Phase 2: Domain & Infrastructure

- [x] 2.1 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/domain/UserRole.kt` enum (`ADMIN`, `AGENT`, `PSYCHOLOGIST`)
- [x] 2.2 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/domain/User.kt` JPA entity with UUID PK, email, passwordHash, role, isActive, timestamps
- [x] 2.3 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/infrastructure/UserRepository.kt` extending `JpaRepository<User, UUID>` with `findByEmail`

**Verification**: `./gradlew bootRun` starts with `ddl-auto=validate`; Hibernate maps table correctly

## Phase 3: Security Infrastructure

- [x] 3.1 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/shared/config/JwtProperties.kt` `@ConfigurationProperties`
- [x] 3.2 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/shared/security/JwtTokenProvider.kt` with `generateToken`, `validateToken`, `getAuthentication`
- [x] 3.3 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/shared/security/CustomUserDetailsService.kt` loading `UserDetails` by email
- [x] 3.4 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/shared/security/JwtAuthenticationFilter.kt` extracting `Authorization: Bearer <token>`
- [x] 3.5 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/shared/config/SecurityConfig.kt` with `SecurityFilterChain`, `BCryptPasswordEncoder` bean, `@EnableMethodSecurity`

**Verification**: Unauthenticated `GET /users` returns 401; `POST /auth/login` with admin seed returns 200

## Phase 4: Application Layer

- [x] 4.1 Create DTOs in `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/application/dto/`: `LoginRequest`, `TokenResponse`, `CreateUserRequest`, `UpdateUserRequest`, `UserResponse`
- [x] 4.2 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/application/AuthService.kt` with `login(email, password)` and `refresh(refreshToken)`
- [x] 4.3 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/application/UserService.kt` with CRUD operations and role checks

**Verification**: `AuthService.login("admin@gmail.com", "123456")` returns valid tokens; `UserService.createUser` persists with BCrypt hash

## Phase 5: API Layer

- [x] 5.1 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/api/AuthController.kt` exposing `POST /auth/login` and `POST /auth/refresh`
- [x] 5.2 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/user/api/UserController.kt` exposing `GET/POST/PUT/DELETE /users` with `@PreAuthorize`
- [x] 5.3 Create `fpc-back/src/main/kotlin/com/hazardev/fpc_back/shared/exception/GlobalExceptionHandler.kt` returning structured `ErrorResponse` for 400/401/403/404/500

**Verification**: `POST /auth/login` returns JSON with `accessToken` and `refreshToken`; `POST /users` without ADMIN token returns 403

## Phase 6: Testing

- [x] 6.1 Write `JwtTokenProviderTest` — unit test token generation, validation, expiration, and claim extraction
- [x] 6.2 Write `AuthServiceTest` — unit test login success/failure and refresh with mocked `UserRepository` and `BCryptPasswordEncoder`
- [x] 6.3 Write `UserServiceTest` — unit test CRUD and role enforcement with mocked repository
- [x] 6.4 Write `AuthControllerIntegrationTest` — integration test login, refresh, and invalid credentials using `@SpringBootTest`
- [x] 6.5 Write `UserControllerIntegrationTest` — integration test CRUD and role-based access with JWT tokens

**Verification**: `./gradlew test` passes with green suite; integration tests confirm 401/403 behavior per spec

## Phase 7: Final Verification

- [x] 7.1 Run `./gradlew bootRun` and verify admin seed exists in database
- [x] 7.2 Verify `POST /auth/login` returns tokens for `admin@gmail.com` / `123456`
- [x] 7.3 Verify `GET /users` requires Bearer token and `POST /users` requires ADMIN role
- [x] 7.4 Verify password hashes in DB are not plaintext
- [x] 7.5 Verify `./gradlew test` passes all unit and integration tests
