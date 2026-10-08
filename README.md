# BIT-41: Event-Driven Commerce Fulfillment Platform

A production-quality prototype of an event-driven microservices architecture implementing the **Saga Pattern (Choreography)** for distributed transactions. 

Built with Java 21, Spring Boot 3, PostgreSQL, RabbitMQ, and React.

## System Architecture

The platform consists of 5 backend microservices and 1 frontend UI, communicating asynchronously via RabbitMQ:
1. **Catalog Service** (`:8081`): Manages product metadata.
2. **Inventory Service** (`:8082`): Manages stock levels and executes stock reservations.
3. **Order Service** (`:8083`): Ingress for user checkouts. Initiates the Saga.
4. **Payment Service** (`:8084`): Simulates payment processing with a configurable failure rate to test compensating transactions.
5. **Notification Service** (`:8085`): Simulates email/SMS delivery for terminal Saga states.
6. **Admin Dashboard (React)** (`:80`): Live monitoring UI.

## Quick Start (Docker)

The easiest way to run the entire stack is via Docker Compose. Ensure you have Docker and Docker Compose installed.

```bash
# Boot the entire stack (Postgres, RabbitMQ, 5x Java Services, 1x React UI)
docker-compose up -d --build
```



## Key Features
- **Saga Choreography:** Decentralized workflow without a single orchestrator bottleneck.
- **Compensating Transactions:** If payment fails, an event is broadcasted to release reserved inventory automatically.
- **Idempotency Guarantee:** Every consumer uses a `processed_events` PostgreSQL table to prevent duplicate message side-effects.
- **Distributed Tracing:** Micrometer & Logstash inject `traceId` and `spanId` across HTTP and RabbitMQ bounds for centralized observability.

## Load Testing
A k6 script is provided to simulate concurrent users triggering race conditions.
```bash
k6 run load-tests/saga-load-test.js
```

## Documentation
- [Architecture & Sequence Diagrams](docs/architecture.md)
- [Database Schemas](docs/db/database_schema.sql)
- [Evaluation Report & Baseline Comparison](docs/evaluation_report.md)
- [STRIDE Threat Model](docs/threat_model.md)
