package com.bit41.notificationservice.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
public class ProcessedEvent {
    @Id
    private UUID eventId;
    private UUID sagaId;
    private String eventType;
    private LocalDateTime processedAt = LocalDateTime.now();

    public ProcessedEvent() {}
    public ProcessedEvent(UUID eventId, UUID sagaId, String eventType) {
        this.eventId = eventId;
        this.sagaId = sagaId;
        this.eventType = eventType;
    }
    public UUID getEventId() { return eventId; }
    public void setEventId(UUID eventId) { this.eventId = eventId; }
}
