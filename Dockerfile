FROM gradle:8.12.1-jdk21 AS builder

WORKDIR /workspace

COPY gradlew .
COPY gradle/wrapper/ gradle/wrapper/
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon -q

COPY src/ src/
RUN chmod +x gradlew && ./gradlew bootJar --no-daemon

FROM eclipse-temurin:21-jre-noble

RUN groupadd --system appgroup && useradd --system --no-create-home --gid appgroup appuser

WORKDIR /app

COPY --from=builder /workspace/build/libs/fpc-back-0.0.1-SNAPSHOT.jar app.jar

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", \
    "-Djava.security.egd=file:/dev/./urandom", \
    "-jar", \
    "app.jar" \
]