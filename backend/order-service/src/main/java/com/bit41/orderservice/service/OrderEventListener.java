package com.bit41.orderservice.service;

import com.bit41.orderservice.dto.SagaEvent;
import com.bit41.orderservice.model.Order;
import com.bit41.orderservice.model.ProcessedEvent;
import com.bit41.orderservice.repository.OrderRepository;
import com.bit41.orderservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class OrderEventListener {

    private static final Logger log = LoggerFactory.getLogger(OrderEventListener.class);
    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderEventListener(OrderRepository orderRepository, ProcessedEventRepository processedEventRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "order.queue")
    @Transactional
    public void handleOrderEvent(SagaEvent event) {
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        log.info("Received event {} for order {}", event.getEventType(), event.getOrderId());
        Optional<Order> orderOpt = orderRepository.findById(event.getOrderId());
        if (orderOpt.isEmpty()) {
            log.warn("Order not found: {}", event.getOrderId());
            return;
        }
        
        Order order = orderOpt.get();

        if ("PaymentSucceeded".equals(event.getEventType())) {
            order.setStatus("CONFIRMED");
            orderRepository.save(order);
            // Notify customer
            rabbitTemplate.convertAndSend("saga.exchange", "notification.send", 
                new SagaEvent(event.getSagaId(), order.getId(), "OrderConfirmed", null));
                
        } else if ("PaymentFailed".equals(event.getEventType()) || "InventoryReservationFailed".equals(event.getEventType())) {
            order.setStatus("CANCELLED");
            orderRepository.save(order);
            // Notify customer
            rabbitTemplate.convertAndSend("saga.exchange", "notification.send", 
                new SagaEvent(event.getSagaId(), order.getId(), "OrderCancelled", null));
        }

        processedEventRepository.save(new ProcessedEvent(event.getEventId(), event.getSagaId(), event.getEventType()));
    }
}
