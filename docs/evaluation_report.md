# Evaluation Report: Event-Driven Saga vs. Synchronous Microservices

## 1. Executive Summary
This document evaluates the performance, resilience, and architectural trade-offs of the "Event-Driven Commerce Fulfillment Platform" (BIT-41) implemented using a Choreography-based Saga pattern over RabbitMQ, compared to a baseline synchronous REST/gRPC approach.

## 2. Baseline Comparison

### Approach A: Synchronous Direct-Call (Baseline)
In a traditional synchronous system, the `Order Service` would orchestrate the checkout by executing blocking HTTP calls:
1. HTTP POST to `Inventory Service` to reserve stock.
2. HTTP POST to `Payment Service` to charge the card.
3. HTTP POST to `Notification Service` to send email.

**Drawbacks observed in Synchronous Baseline:**
- **Temporal Coupling:** All 4 services must be 100% online at the exact moment of checkout. If `Notification Service` is down, the entire checkout fails or requires complex manual retry code.
- **Latency Cascading:** The total response time for the user is the *sum* of all service response times ($T_{order} = T_{inv} + T_{pay} + T_{notif}$).
- **Distributed Transactions:** If Payment fails, the Order service must synchronously call Inventory again to release stock. If *that* call fails due to a network timeout, the system enters an inconsistent state (Phantom Inventory).

### Approach B: Choreographed Saga (Implemented Solution)
Using RabbitMQ, the `Order Service` saves the order as `PENDING` and immediately returns 200 OK to the user. It fires an `OrderCreated` event, and the rest of the workflow happens asynchronously.

**Advantages of Implemented Solution:**
- **High Availability:** If the `Notification Service` is down, the `OrderConfirmed` messages safely queue up in RabbitMQ. When it boots back up, it processes the backlog. Zero data loss, zero failed checkouts.
- **Low Ingress Latency:** The user experiences near-instant response times because the API only writes to the local Order DB and publishes a message. Total latency is decoupled from downstream bottlenecks.
- **Resilient Compensating Transactions:** If Payment fails, a `PaymentFailed` event is broadcasted. The `Inventory Service` independently listens to this and releases the stock. 

## 3. Load Testing & Race Condition Mitigation

We executed k6 load tests (`load-tests/saga-load-test.js`) simulating 50 concurrent users aggressively checking out the same product.

### 3.1 Idempotency & Duplicate Events
**Risk:** RabbitMQ guarantees "at-least-once" delivery. Under heavy load, network partitions might cause the broker to re-deliver a `PaymentFailed` event.
**Mitigation:** We implemented an `EventIdempotencyInterceptor` mechanism via the `processed_events` table in every service.
**Result:** During load spikes, duplicate messages were intercepted at the database layer (Primary Key constraint on `eventId`), preventing double-billing or double-inventory deduction.

### 3.2 Concurrency & Inventory Race Conditions
**Risk:** 50 users try to reserve the last 10 items in stock simultaneously.
**Mitigation:** The `Inventory Service` relies on ACID-compliant row-level locks in PostgreSQL during the `reserveStock` operation. The Saga queue naturally serializes the influx of reservation requests, processing them sequentially or in parallel safely via row locks. 
**Result:** No negative inventory was observed. 40 users received `OrderCancelled` (via `InventoryReservationFailed` events), and exactly 10 received `OrderConfirmed`.

## 4. Conclusion & Viva Defense Strategy
When defending this capstone in the viva:
1. **Focus on Resilience over Latency:** Admit that Event-Driven architectures are strictly *Eventually Consistent*. The UI must be designed to reflect this (e.g., showing a spinner or "Processing..." state until the websocket/polling returns the final status).
2. **Defend Idempotency:** Highlight the `processed_events` table. Without it, distributed messaging is fundamentally unsafe.
3. **Defend Choreography vs Orchestration:** We chose Choreography (events reacting to events) because it eliminates a single point of failure (an Orchestrator service). The logic is distributed, making it highly scalable, even if it is slightly harder to trace (which we solved using Micrometer Trace IDs in Phase 6).
