package com.bit41.orderservice.service;

import com.bit41.orderservice.dto.SagaEvent;
import com.bit41.orderservice.model.Order;
import com.bit41.orderservice.model.OrderItem;
import com.bit41.orderservice.repository.OrderRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final RabbitTemplate rabbitTemplate;

    public OrderService(OrderRepository orderRepository, RabbitTemplate rabbitTemplate) {
        this.orderRepository = orderRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @Transactional
    public Order createOrder(Order order) {
        order.setStatus("PENDING");
        
        BigDecimal total = BigDecimal.ZERO;
        for (OrderItem item : order.getItems()) {
            item.setOrder(order);
            total = total.add(item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }
        order.setTotalAmount(total);
        
        Order savedOrder = orderRepository.save(order);
        
        UUID sagaId = UUID.randomUUID();
        Map<String, Object> payload = new HashMap<>();
        payload.put("amount", total);
        
        // Pass products to reserve
        Map<String, Integer> itemsMap = new HashMap<>();
        for (OrderItem item : order.getItems()) {
            itemsMap.put(item.getProductId().toString(), item.getQuantity());
        }
        payload.put("items", itemsMap);

        rabbitTemplate.convertAndSend("saga.exchange", "inventory.reserve", 
            new SagaEvent(sagaId, savedOrder.getId(), "OrderCreated", payload));
        
        return savedOrder;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrdersByCustomerId(UUID customerId) {
        return orderRepository.findByCustomerId(customerId);
    }

    public Optional<Order> getOrderById(UUID id) {
        return orderRepository.findById(id);
    }
}
