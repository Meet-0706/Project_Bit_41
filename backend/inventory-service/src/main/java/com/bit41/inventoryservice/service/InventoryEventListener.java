package com.bit41.inventoryservice.service;

import com.bit41.inventoryservice.dto.SagaEvent;
import com.bit41.inventoryservice.model.ProcessedEvent;
import com.bit41.inventoryservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
public class InventoryEventListener {

    private static final Logger log = LoggerFactory.getLogger(InventoryEventListener.class);
    private final InventoryService inventoryService;
    private final ProcessedEventRepository processedEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public InventoryEventListener(InventoryService inventoryService, ProcessedEventRepository processedEventRepository, RabbitTemplate rabbitTemplate) {
        this.inventoryService = inventoryService;
        this.processedEventRepository = processedEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "inventory.queue")
    @Transactional
    public void handleInventoryEvent(SagaEvent event) {
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        log.info("Received event {} for order {}", event.getEventType(), event.getOrderId());
        
        if ("OrderCreated".equals(event.getEventType())) {
            Map<String, Integer> items = (Map<String, Integer>) event.getPayload().get("items");
            boolean allReserved = true;
            
            // Basic reservation loop. Real systems would handle partial rollbacks if one fails.
            for (Map.Entry<String, Integer> entry : items.entrySet()) {
                boolean reserved = inventoryService.reserveStock(UUID.fromString(entry.getKey()), entry.getValue());
                if (!reserved) {
                    allReserved = false;
                    break; // simplistic failure
                }
            }
            
            if (allReserved) {
                rabbitTemplate.convertAndSend("saga.exchange", "payment.process", 
                    new SagaEvent(event.getSagaId(), event.getOrderId(), "InventoryReserved", event.getPayload()));
            } else {
                rabbitTemplate.convertAndSend("saga.exchange", "order.cancel", 
                    new SagaEvent(event.getSagaId(), event.getOrderId(), "InventoryReservationFailed", event.getPayload()));
            }
            
        } else if ("PaymentFailed".equals(event.getEventType()) || "OrderCancelled".equals(event.getEventType())) {
            // Compensating action
            Map<String, Integer> items = (Map<String, Integer>) event.getPayload().get("items");
            if (items != null) {
                for (Map.Entry<String, Integer> entry : items.entrySet()) {
                    inventoryService.releaseStock(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }
            log.info("Released inventory for order {}", event.getOrderId());
        } else if ("OrderConfirmed".equals(event.getEventType())) {
            Map<String, Integer> items = (Map<String, Integer>) event.getPayload().get("items");
            if (items != null) {
                for (Map.Entry<String, Integer> entry : items.entrySet()) {
                    inventoryService.confirmStock(UUID.fromString(entry.getKey()), entry.getValue());
                }
            }
        }

        processedEventRepository.save(new ProcessedEvent(event.getEventId(), event.getSagaId(), event.getEventType()));
    }
}
