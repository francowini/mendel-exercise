# Mendel Transaction Service

Spring Boot service for managing transactions.

## Running

```bash
./gradlew bootRun
```

Runs on http://localhost:8080

## Running with Docker

Start the service:

```bash
docker-compose up
```

The service will be available at http://localhost:8080

Rebuild after code changes:

```bash
docker-compose up --build
```

Stop:

```bash
docker-compose down
```

**Note:** The Docker images support both ARM (Apple Silicon) and x86 architectures.
