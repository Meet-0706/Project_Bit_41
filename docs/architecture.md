# Architecture & Saga Orchestration

## 1. C4 Context Diagram

```mermaid
C4Context
    title System Context diagram for Event-Driven Commerce Platform

    Person(customer, "Customer", "A customer of the e-commerce store")
    Person(admin, "Admin", "Store administrator")

    System(commerce_platform, "Commerce Platform", "Allows customers to place orders, and admins to manage inventory and view orders.")

    System_Ext(payment_gateway, "External Payment Gateway", "Mocked payment provider")
    System_Ext(email_sys, "Email System", "Sends notifications to customers")

    Rel(customer, commerce_platform, "Browses products, places orders", "HTTPS")
    Rel(admin, commerce_platform, "Manages system, views dashboard", "HTTPS")
    Rel(commerce_platform, payment_gateway, "Processes payments", "HTTPS/Events")
    Rel(commerce_platform, email_sys, "Sends order confirmations", "SMTP/API")
```

## 2. C4 Container Diagram

```mermaid
C4Container
    title Container diagram for Commerce Platform

    Person(customer, "Customer", "A customer")
    Person(admin, "Admin", "An admin")

    Container(spa, "Single Page App", "React", "Provides the storefront and admin dashboard")
    
    Container_Boundary(backend, "Backend Microservices") {
        Container(api_gateway, "API Gateway", "Spring Cloud Gateway", "Routes requests, handles auth (JWT)")
        Container(catalog_svc, "Catalog Service", "Spring Boot, Java 21", "Product info")
        Container(order_svc, "Order Service", "Spring Boot, Java 21", "Cart and order management")
        Container(inventory_svc, "Inventory Service", "Spring Boot, Java 21", "Stock reservation")
        Container(payment_svc, "Payment Mock Service", "Spring Boot, Java 21", "Simulates payments")
        Container(notification_svc, "Notification Service", "Spring Boot, Java 21", "Customer notifications")
    }

    ContainerDb(db_catalog, "Catalog DB", "PostgreSQL", "Stores products")
    ContainerDb(db_order, "Order DB", "PostgreSQL", "Stores orders")
    ContainerDb(db_inventory, "Inventory DB", "PostgreSQL", "Stores stock levels")
    ContainerDb(db_payment, "Payment DB", "PostgreSQL", "Stores payment records")

    Container(rabbitmq, "Message Broker", "RabbitMQ", "Event bus for Saga orchestration")

    Rel(customer, spa, "Visits", "HTTPS")
    Rel(admin, spa, "Manages", "HTTPS")
    
    Rel(spa, api_gateway, "Makes API calls to", "JSON/HTTPS")
    
    Rel(api_gateway, catalog_svc, "Routes to", "HTTP")
    Rel(api_gateway, order_svc, "Routes to", "HTTP")
    
    Rel(catalog_svc, db_catalog, "Reads/Writes", "JDBC")
    Rel(order_svc, db_order, "Reads/Writes", "JDBC")
    Rel(inventory_svc, db_inventory, "Reads/Writes", "JDBC")
    Rel(payment_svc, db_payment, "Reads/Writes", "JDBC")

    Rel(order_svc, rabbitmq, "Publishes OrderCreated", "AMQP")
    Rel(inventory_svc, rabbitmq, "Consumes OrderCreated, Publishes InventoryReserved/Failed", "AMQP")
    Rel(payment_svc, rabbitmq, "Consumes InventoryReserved, Publishes PaymentSucceeded/Failed", "AMQP")
    Rel(order_svc, rabbitmq, "Consumes Payment events, Publishes OrderConfirmed/Cancelled", "AMQP")
    Rel(notification_svc, rabbitmq, "Consumes terminal order events", "AMQP")
```

## 3. Sequence Diagrams (Saga Pattern)

### A. Happy-Path Order Flow

```mermaid
sequenceDiagram
    participant C as Client (React)
    participant O as Order Service
    participant RMQ as RabbitMQ
    participant I as Inventory Service
    participant P as Payment Service
    participant N as Notification Service

    C->>O: POST /api/orders (cart items)
    O-->>C: 201 Created (Order ID, Status: PENDING)
    O->>RMQ: Publish [OrderCreatedEvent]
    
    RMQ-->>I: Consume [OrderCreatedEvent]
    I->>I: Check stock & reserve inventory
    I->>RMQ: Publish [InventoryReservedEvent]
    
    RMQ-->>P: Consume [InventoryReservedEvent]
    P->>P: Process Mock Payment (Success)
    P->>RMQ: Publish [PaymentSucceededEvent]
    
    RMQ-->>O: Consume [PaymentSucceededEvent]
    O->>O: Update Order Status -> CONFIRMED
    O->>RMQ: Publish [OrderConfirmedEvent]
    
    RMQ-->>N: Consume [OrderConfirmedEvent]
    N->>N: Send Email
```

### B. Payment-Failure Rollback Flow (Compensating Transaction)

```mermaid
sequenceDiagram
    participant C as Client (React)
    participant O as Order Service
    participant RMQ as RabbitMQ
    participant I as Inventory Service
    participant P as Payment Service

    C->>O: POST /api/orders (cart items)
    O->>RMQ: Publish [OrderCreatedEvent]
    
    RMQ-->>I: Consume [OrderCreatedEvent]
    I->>I: Reserve inventory
    I->>RMQ: Publish [InventoryReservedEvent]
    
    RMQ-->>P: Consume [InventoryReservedEvent]
    P->>P: Process Mock Payment (Failure)
    P->>RMQ: Publish [PaymentFailedEvent]
    
    RMQ-->>O: Consume [PaymentFailedEvent]
    O->>O: Update Order Status -> CANCELLED
    O->>RMQ: Publish [OrderCancelledEvent]
    
    RMQ-->>I: Consume [OrderCancelledEvent]
    I->>I: Release reserved inventory (Compensating Action)
```

### C. Duplicate-Event Idempotency Flow

```mermaid
sequenceDiagram
    participant RMQ as RabbitMQ
    participant S as Any Consumer Service
    participant DB as Service Database

    RMQ-->>S: Deliver [Event 123] (First time)
    S->>DB: Check if Event 123 in processed_events?
    DB-->>S: Not found
    S->>DB: Begin Transaction
    S->>DB: Execute Business Logic (e.g. deduct stock)
    S->>DB: Insert Event 123 to processed_events
    S->>DB: Commit Transaction
    S-->>RMQ: Ack Message
    
    Note over RMQ, S: Network issue causes message redelivery
    
    RMQ-->>S: Deliver [Event 123] (Duplicate)
    S->>DB: Check if Event 123 in processed_events?
    DB-->>S: Found!
    S->>S: Skip business logic
    S-->>RMQ: Ack Message (Safe to ignore)
```
