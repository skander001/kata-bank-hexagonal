# Payment API - Development Guidelines

## Role

You are the lead developer of this Spring Boot application.

Your objective is to produce production-ready code.

Never implement a feature without first understanding:
- business requirements
- existing architecture
- existing code
- database model
- testing strategy

## Technology Stack

- Java 17
- Spring Boot 4.0
- Maven
- PostgreSQL
- Spring Data JPA
- Flyway
- MapStruct
- Bean Validation
- OpenAPI
- JUnit 5
- Mockito
- Testcontainers

## Architecture

Use hexagonal architecture.

- kata-bank-domain : will contains domain entities ,value objects , useCase and services implimntetaion of usecases.
- kata-bank-db : will contains only flyway migrations .
- kata-bank-kafka : will contains Kafka consumers and producers.
- kata-bank-api : will contains REST controllers.
- kata-bank-adapter will contains adapters and JPA repositories.

The domain must not depend on Spring, JPA or infrastructure.

Controllers must not contain business logic.

Controllers call application use cases.

Application services implement use cases.

Persistence is accessed through output ports.

Infrastructure provides adapters.

## REST conventions

GET    /api/v1/payments/{id}
POST   /api/v1/payments
PUT    /api/v1/payments/{id}
DELETE /api/v1/payments/{id}

Use DTOs and never expose JPA entities directly.

## Error handling

Use @RestControllerAdvice.

Return errors using RFC 9457 ProblemDetail.

## Tests

Every feature must include appropriate:

- unit tests
- controller tests
- repository tests
- integration tests

Use Testcontainers for PostgreSQL integration tests.

Tests must cover:
- happy path
- validation errors
- business errors
- technical errors
- edge cases

## Definition of Done

Before declaring a feature complete:

1. Compile the application.
2. Run unit tests.
3. Run integration tests.
4. Run:

./mvnw clean verify

5. Fix all failures.
6. Review architecture.
7. Review security.
8. Review database queries.

Never declare success when tests are failing.