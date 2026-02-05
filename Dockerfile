# Multi-stage Docker build for KeepLynk AI Engine
# Root Dockerfile that references ai-engine subdirectory

# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Maven wrapper and pom.xml from ai-engine subdirectory
COPY ai-engine/mvnw .
COPY ai-engine/.mvn .mvn
COPY ai-engine/pom.xml .

# Make Maven wrapper executable
RUN chmod +x mvnw

# Download dependencies (cached layer)
RUN ./mvnw dependency:go-offline -B

# Copy source code from ai-engine subdirectory
COPY ai-engine/src ./src

# Build the application (skip tests for faster builds)
RUN ./mvnw clean package -DskipTests

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine

# Install curl for health checks
RUN apk add --no-cache curl

# Create non-root user for security
RUN addgroup -g 1001 spring && adduser -u 1001 -G spring -s /bin/sh -D spring

# Set working directory
WORKDIR /app

# Copy the JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to non-root user
RUN chown -R spring:spring /app

# Switch to non-root user
USER spring

# Expose port (default 8081, but configurable via PORT env var)
EXPOSE 8081

# Health check - Railway automatically sets PORT env var
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD curl -f http://localhost:${PORT:-8081}/actuator/health || exit 1

# JVM options for container environments
ENV JAVA_OPTS="-Xmx512m -Xms256m -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Run the application - Spring Boot reads PORT from environment
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
