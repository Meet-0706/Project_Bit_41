package com.bit41.inventoryservice.service;

import com.bit41.inventoryservice.dto.SagaEvent;
import com.bit41.inventoryservice.model.Stock;
import com.bit41.inventoryservice.repository.ProcessedEventRepository;
import com.bit41.inventoryservice.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import com.bit41.inventoryservice.InventoryServiceApplication;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = InventoryServiceApplication.class, properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;INIT=CREATE SCHEMA IF NOT EXISTS inventory",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class IdempotencyTest {

    @Autowired
    private InventoryEventListener inventoryEventListener;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @MockitoBean
    private RabbitTemplate rabbitTemplate; // Mock so we don't try to connect to real RabbitMQ

    private UUID productId;

    @BeforeEach
    void setUp() {
        stockRepository.deleteAll();
        processedEventRepository.deleteAll();
        
        productId = UUID.randomUUID();
        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setAvailableQuantity(10);
        stock.setReservedQuantity(0);
        stockRepository.save(stock);
    }

    @Test
    void testIdempotency_DuplicateOrderCreatedEvent() {
        SagaEvent event = new SagaEvent();
        event.setEventId(UUID.randomUUID());
        event.setSagaId(UUID.randomUUID());
        event.setOrderId(UUID.randomUUID());
        event.setEventType("OrderCreated");
        
        Map<String, Object> payload = new HashMap<>();
        Map<String, Integer> items = new HashMap<>();
        items.put(productId.toString(), 2);
        payload.put("items", items);
        event.setPayload(payload);

        // First delivery
        inventoryEventListener.handleInventoryEvent(event);
        
        Stock stockAfterFirst = stockRepository.findById(productId).get();
        assertEquals(8, stockAfterFirst.getAvailableQuantity());
        assertEquals(2, stockAfterFirst.getReservedQuantity());
        assertEquals(1, processedEventRepository.count());

        // Second delivery (Duplicate)
        inventoryEventListener.handleInventoryEvent(event);
        
        Stock stockAfterSecond = stockRepository.findById(productId).get();
        // The quantities should remain unchanged
        assertEquals(8, stockAfterSecond.getAvailableQuantity());
        assertEquals(2, stockAfterSecond.getReservedQuantity());
        // The processed events table should still have 1 entry
        assertEquals(1, processedEventRepository.count());
    }
}
