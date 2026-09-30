package com.bit41.paymentservice.dto;

import java.util.UUID;
import java.util.Map;

public class SagaEvent {
    private UUID eventId = UUID.randomUUID();
    private UUID sagaId;
    private UUID orderId;
    private String eventType;
    private Map<String, Object> payload;

    public SagaEvent() {}
    public SagaEvent(UUID sagaId, UUID orderId, String eventType, Map<String, Object> payload) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.eventType = eventType;
        this.payload = payload;
    }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
    public UUID getSagaId() { return sagaId; }
    public void setSagaId(UUID sagaId) { this.sagaId = sagaId; }
    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Map<String, Object> getPayload() { return payload; }
    public void setPayload(Map<String, Object> payload) { this.payload = payload; }
}
