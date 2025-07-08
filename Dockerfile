# Multi-stage build
FROM openjdk:11-jdk-slim as builder

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle/ ./gradle/
COPY gradlew ./

# Copy source code
COPY common/ ./common/
COPY notification-api/ ./notification-api/
COPY message-processor/ ./message-processor/
COPY delivery-engine/ ./delivery-engine/

# Build the application
RUN ./gradlew clean build -x test

# Runtime stage with outdated JRE
FROM openjdk:11-jre-slim

WORKDIR /app

# Create non-root user
RUN groupadd -r notification && useradd -r -g notification notification

# Copy the built JARs
COPY --from=builder /app/notification-api/build/libs/notification-api-*.jar notification-api.jar
COPY --from=builder /app/message-processor/build/libs/message-processor-*.jar message-processor.jar

# Create logs directory
RUN mkdir -p logs && chown notification:notification logs

# Expose port
EXPOSE 8080 8081

# Switch to non-root user
USER notification

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8081/healthcheck || exit 1

# Run the notification API by default
ENTRYPOINT ["java", "-jar", "notification-api.jar", "server", "application.yml"]