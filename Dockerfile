# ── Stage 1: Build ───────────────────────────────────────────────────────────
FROM gradle:8.11.1-jdk21 AS build

WORKDIR /app

# Resolve dependencies before copying source (layer cache)
COPY build.gradle.kts settings.gradle.kts ./
RUN gradle dependencies --no-daemon -q || true

# Copy source and build (production Vaadin bundle included)
COPY src/ src/
ENV VAADIN_PRODUCTION_MODE=true
RUN gradle build -x test --no-daemon

# ── Stage 2: Runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy

WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
