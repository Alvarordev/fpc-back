# ── Stage 1: Builder ────────────────────────────────────────────
FROM gradle:8.12.1-jdk21 AS builder

WORKDIR /workspace

# Copy Gradle wrapper and configuration (leverage layer caching)
COPY gradlew .
COPY gradle/wrapper/ gradle/wrapper/
COPY build.gradle.kts .
COPY settings.gradle.kts .

# Warm the Gradle cache by downloading dependencies first
RUN ./gradlew dependencies --no-daemon -q

# Copy source and build
COPY src/ src/
RUN ./gradlew bootJar --no-daemon

# ── Stage 2: Runtime ────────────────────────────────────────────
FROM eclipse-temurin:21-jre-noble

# Create non-root user
RUN groupadd --system appgroup && useradd --system --no-create-home --gid appgroup appuser

WORKDIR /app

# Copy the fat JAR from builder
COPY --from=builder /workspace/build/libs/fpc-back-0.0.1-SNAPSHOT.jar app.jar

# Switch to non-root user
USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar" \
]
