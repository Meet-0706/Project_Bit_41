# STRIDE Threat Model

This document outlines the threat modeling for the Event-Driven Saga Platform using the STRIDE methodology.

## 1. Spoofing (Identity spoofing)
**Threat:** A malicious actor spoofs the Order Service and injects fake `OrderCreated` events directly into RabbitMQ to reserve all available inventory (Denial of Inventory).
**Mitigation:** 
- **Network Isolation:** RabbitMQ is isolated within the internal Docker network. External traffic is blocked.
- **Authentication:** In a production deployment, RabbitMQ should use TLS and require strong credentials for service-to-service publishing.
- **JWT Auth:** The ingress API Gateway (future enhancement) must validate JWTs before allowing the Order API to trigger the Saga.

## 2. Tampering (Data tampering)
**Threat:** An attacker intercepts the RabbitMQ message in transit and changes the `amount` payload before it reaches the Payment Service.
**Mitigation:**
- **TLS/SSL:** Enforce AMQPS (TLS for RabbitMQ) for data-in-transit encryption.
- **Immutable Events:** The payload itself should ideally be signed (HMAC) by the publishing service if zero-trust architecture is required, though TLS within a VPC is usually sufficient.

## 3. Repudiation
**Threat:** The Payment Service charges a user, but the Order Service crashes before receiving the `PaymentSucceeded` event, leaving the order in `PENDING`. The system cannot prove the payment happened in relation to the order.
**Mitigation:** 
- **Traceability:** Every event contains a unique `eventId` and `sagaId`, logged to structured JSON with Logback.
- **State Auditing:** The Payment Service stores a permanent `Transaction` record in `payment_db` linked to the `orderId`. This can be reconciled against the Order database in an audit.

## 4. Information Disclosure
**Threat:** Sensitive customer data (e.g., credit card numbers) is leaked via RabbitMQ events or application logs.
**Mitigation:**
- **Data Minimization:** Events only contain UUIDs (`customerId`, `orderId`) and monetary amounts. Real PII or PCI data is never serialized into the RabbitMQ payload.
- **Log Masking:** Logback configurations must be set to mask any sensitive headers if they are accidentally logged.

## 5. Denial of Service (DoS)
**Threat:** A flood of checkout requests overwhelms the system, crashing the backend services and corrupting the database.
**Mitigation:**
- **Asynchronous Ingress:** The `Order Service` simply writes to the DB and publishes an event, returning 200 OK rapidly. It can handle massive spikes.
- **Consumer Throttling:** The `Inventory` and `Payment` services pull messages from RabbitMQ at their own pace based on their `prefetch_count`. They will not crash under load; the queue will simply grow (absorbing the shock).

## 6. Elevation of Privilege
**Threat:** A user gains access to the Admin Dashboard and manipulates the Payment Failure Rate or views internal stock.
**Mitigation:**
- **RBAC (Role-Based Access Control):** The frontend and configuration APIs (`/api/payment/config`) currently lack authentication in this prototype. In a real-world scenario, Spring Security must be added, requiring a JWT with the `ROLE_ADMIN` authority to access these endpoints.
