# ============================================================
# Multi-stage Dockerfile for EventHive Spring Boot backend
# ============================================================

# ─── Stage 1: Build ───
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom first to leverage Docker layer caching for dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# ─── Stage 2: Runtime ───
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Render injects PORT env var; Spring Boot needs to listen on it
ENV PORT=8080
EXPOSE 8080

# Use exec form so signals (SIGTERM) reach the JVM properly
ENTRYPOINT ["java", "-jar", "app.jar"]