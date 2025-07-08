# Notification Service

A multi-module Dropwizard application for managing and delivering notifications across multiple channels.

## Overview

This service provides a comprehensive notification system with:
- **notification-api**: REST API for notification management
- **message-processor**: Background service for processing notifications
- **delivery-engine**: Channel-specific delivery implementations
- **common**: Shared models and utilities

## Technology Stack

- **Framework**: Dropwizard 2.1.x
- **Java**: Java 14
- **Build Tool**: Gradle 7.6
- **Database**: PostgreSQL with JDBI
- **Queue**: Redis for message queuing
- **Authentication**: JWT + API Key
- **Logging**: Log4j2
- **Testing**: JUnit 5

## Notification Channels

- **EMAIL**: Email notifications with attachments
- **SMS**: Text message notifications
- **PUSH**: Mobile push notifications
- **SLACK**: Slack channel/direct messages
- **WEBHOOK**: HTTP webhook notifications

## API Endpoints

### Authentication
- JWT Token: `Authorization: Bearer <token>`
- API Key: `X-API-Key: <key>`

### Notification API (`/api/v1/notifications`)

#### Create Notification
```bash
POST /api/v1/notifications
Content-Type: application/json

{
  "title": "Welcome!",
  "message": "Welcome to our platform",
  "channel": "EMAIL",
  "priority": "HIGH",
  "recipientId": "user123",
  "recipientEmail": "user@example.com",
  "templateId": "welcome-template"
}
```

#### Get Notification
```bash
GET /api/v1/notifications/{id}
```

#### Get User Notifications
```bash
GET /api/v1/notifications/recipient/{recipientId}?offset=0&limit=20
```

#### Get Notifications by Status
```bash
GET /api/v1/notifications/status/{status}?limit=100
```

#### Update Notification Status
```bash
PUT /api/v1/notifications/{id}/status
Content-Type: application/x-www-form-urlencoded

status=SENT&errorMessage=
```

#### Retry Failed Notification
```bash
POST /api/v1/notifications/{id}/retry
```

#### Get Statistics
```bash
GET /api/v1/notifications/stats
```

### Batch API (`/api/v1/batches`)

#### Create Batch
```bash
POST /api/v1/batches
Content-Type: application/json

[
  {
    "title": "Batch Message 1",
    "message": "First message",
    "channel": "EMAIL",
    "recipientId": "user1",
    "recipientEmail": "user1@example.com"
  },
  {
    "title": "Batch Message 2", 
    "message": "Second message",
    "channel": "SMS",
    "recipientId": "user2",
    "recipientPhone": "+1234567890"
  }
]
```

#### Get Batch
```bash
GET /api/v1/batches/{batchId}
```

#### Get Batch Statistics
```bash
GET /api/v1/batches/{batchId}/stats
```

## Setup Instructions

### Prerequisites
- Java 14
- PostgreSQL
- Redis
- Gradle 7.6

### Database Setup
1. Create PostgreSQL database:
```sql
CREATE DATABASE notification_db;
CREATE USER notification_user WITH PASSWORD 'notification_pass';
GRANT ALL PRIVILEGES ON DATABASE notification_db TO notification_user;
```

2. Run database migrations:
```bash
./gradlew flywayMigrate
```

### Running the Services

1. Build the project:
```bash
./gradlew build
```

2. Run the notification API:
```bash
./gradlew :notification-api:run
```

3. Run the message processor:
```bash
./gradlew :message-processor:run
```

## Configuration

The application uses `application.yml` for configuration:

```yaml
database:
  url: jdbc:postgresql://localhost:5432/notification_db
  user: notification_user
  password: notification_pass

redis:
  host: localhost
  port: 6379
  
jwtSecret: "notification-secret-key-2023"
apiKeys:
  - "notif-api-key-001"
  - "notif-api-key-002"

retryConfig:
  maxRetries: 3
  initialDelayMs: 1000
  backoffMultiplier: 2.0
```

## Docker Deployment

### Build Docker Image
```bash
docker build -t notification-service .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

## Notification Lifecycle

1. **PENDING**: Notification created and queued
2. **PROCESSING**: Being processed by message processor
3. **SENT**: Successfully delivered
4. **FAILED**: Delivery failed (eligible for retry)
5. **CANCELLED**: Manually cancelled
6. **SCHEDULED**: Scheduled for future delivery

## Error Handling & Retry Logic

- Failed notifications are automatically retried up to 3 times
- Exponential backoff with jitter for retry delays
- Failed notifications can be manually retried via API
- Detailed error messages are logged and stored

## Rate Limiting

- Configurable rate limits per minute and hour
- Redis-based rate limiting implementation
- Different limits for different channels

## Health Checks

- Database connectivity: `http://localhost:8081/healthcheck`
- Redis connectivity: `http://localhost:8081/healthcheck`

## Monitoring

- Application metrics: `http://localhost:8081/metrics`
- Notification statistics via API
- Batch processing statistics

## Testing

Run all tests:
```bash
./gradlew test
```

Run specific module tests:
```bash
./gradlew :notification-api:test
```

## API Keys

Default API keys for testing:
- `notif-api-key-001`
- `notif-api-key-002`

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Check PostgreSQL is running
   - Verify connection details in `application.yml`

2. **Redis Connection Failed**
   - Check Redis is running on port 6379
   - Verify Redis configuration

3. **Notifications Not Processing**
   - Check message-processor service is running
   - Verify Redis queue contains messages

4. **Authentication Errors**
   - Ensure correct API key or JWT token
   - Check token expiration

### Logs

Application logs are written to:
- Console output
- `./logs/notification-api.log`

## Architecture

- **notification-api**: Handles HTTP requests, validates input, stores notifications
- **message-processor**: Polls Redis queues, processes notifications
- **delivery-engine**: Channel-specific delivery logic
- **common**: Shared data models and utilities

## Security Features

- JWT-based authentication
- API key authentication
- Input validation
- SQL injection prevention
- Rate limiting

## Performance Features

- Redis-based message queuing
- Asynchronous processing
- Database connection pooling
- Batch processing support
- Efficient indexing strategy

## Contributing

1. Fork the repository
2. Create feature branch
3. Make changes with tests
4. Run tests and ensure they pass
5. Submit pull request