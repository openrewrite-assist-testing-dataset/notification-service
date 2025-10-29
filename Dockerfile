# Multi-stage build
FROM eclipse-temurin:21-jdk-alpine as builder

WORKDIR /app

# Copy gradle files
COPY build.gradle settings.gradle ./
COPY gradle/ ./gradle/
COPY gradlew ./

# Download dependencies
RUN ./gradlew build --no-daemon -x test

# Copy source code
COPY common/ ./common/
COPY notification-api/ ./notification-api/
COPY message-processor/ ./message-processor/

# Build the application
RUN ./gradlew clean build --no-daemon -x test

# Runtime stage
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Create non-root user
RUN apk add --no-cache shadow && \
    groupadd -r notification && \
    useradd -r -g notification notification

# Copy the built JAR
COPY --from=builder /app/notification-api/build/libs/notification-api-*.jar notification-api.jar

# Create logs directory
RUN mkdir -p logs && chown notification:notification logs

# Expose port
EXPOSE 8080 8081

# Switch to non-root user
USER notification

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD wget -q -O /dev/null http://localhost:8081/healthcheck || exit 1

# Run the notification API by default
ENTRYPOINT ["java", "-jar", "notification-api.jar", "server", "application.yml"]