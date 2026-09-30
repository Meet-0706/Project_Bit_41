-- Database Schemas for Event-Driven Commerce Platform

-- ==========================================
-- 1. Catalog Service DB Schema
-- ==========================================
CREATE SCHEMA IF NOT EXISTS catalog;

CREATE TABLE catalog.products (
    id UUID PRIMARY KEY,
    sku VARCHAR(50) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 2. Inventory Service DB Schema
-- ==========================================
CREATE SCHEMA IF NOT EXISTS inventory;

CREATE TABLE inventory.stock (
    product_id UUID PRIMARY KEY,
    available_quantity INT NOT NULL CHECK (available_quantity >= 0),
    reserved_quantity INT NOT NULL DEFAULT 0 CHECK (reserved_quantity >= 0),
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Saga Idempotency & Recovery Table
CREATE TABLE inventory.processed_events (
    event_id UUID PRIMARY KEY,
    saga_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 3. Order Service DB Schema
-- ==========================================
CREATE SCHEMA IF NOT EXISTS orders;

CREATE TABLE orders.orders (
    id UUID PRIMARY KEY,
    customer_id UUID NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, CONFIRMED, CANCELLED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE orders.order_items (
    id UUID PRIMARY KEY,
    order_id UUID REFERENCES orders.orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    price DECIMAL(10, 2) NOT NULL
);

-- Saga Idempotency Table
CREATE TABLE orders.processed_events (
    event_id UUID PRIMARY KEY,
    saga_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 4. Payment Mock Service DB Schema
-- ==========================================
CREATE SCHEMA IF NOT EXISTS payment;

CREATE TABLE payment.transactions (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    status VARCHAR(50) NOT NULL, -- SUCCESS, FAILED
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Saga Idempotency Table
CREATE TABLE payment.processed_events (
    event_id UUID PRIMARY KEY,
    saga_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ==========================================
-- 5. Notification Service DB Schema
-- ==========================================
CREATE SCHEMA IF NOT EXISTS notification;

CREATE TABLE notification.notifications (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL,
    type VARCHAR(50) NOT NULL, -- ORDER_CONFIRMED, ORDER_CANCELLED
    status VARCHAR(50) NOT NULL, -- SENT, FAILED
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Saga Idempotency Table
CREATE TABLE notification.processed_events (
    event_id UUID PRIMARY KEY,
    saga_id UUID NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    processed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
