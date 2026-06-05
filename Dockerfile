# syntax=docker/dockerfile:1

# ---- Build stage: compile the Spring Boot fat jar with the project's wrapper ----
FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

# Resolve dependencies first so this layer caches across source-only changes.
COPY gradlew ./
COPY gradle ./gradle
COPY settings.gradle build.gradle ./
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies >/dev/null 2>&1 || true

# Build the boot jar (tests run in CI separately; skip here for fast images).
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test \
 && cp "$(ls build/libs/*.jar | grep -v -- '-plain' | head -n1)" build/app.jar

# ---- Runtime stage: slim JRE; container-aware heap for the 1 GB VM ----
FROM eclipse-temurin:21-jre-jammy AS runtime
WORKDIR /app
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=60.0 -XX:+UseSerialGC"
COPY --from=build /app/build/app.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
