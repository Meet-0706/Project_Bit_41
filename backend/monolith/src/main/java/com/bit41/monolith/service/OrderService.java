package com.bit41.monolith.service;

import com.bit41.monolith.model.Order;
import com.bit41.monolith.model.OrderItem;
import com.bit41.monolith.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;

    public OrderService(OrderRepository orderRepository, 
                        InventoryService inventoryService,
                        PaymentService paymentService,
                        NotificationService notificationService) {
        this.orderRepository = orderRepository;
        this.inventoryService = inventoryService;
        this.paymentService = paymentService;
        this.notificationService = notificationService;
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
        
        // Synchronous Saga orchestration
        boolean inventoryReserved = true;
        for (OrderItem item : order.getItems()) {
            boolean reserved = inventoryService.reserveStock(item.getProductId(), item.getQuantity());
            if (!reserved) {
                inventoryReserved = false;
                break;
            }
        }
        
        if (!inventoryReserved) {
            order.setStatus("FAILED");
            orderRepository.save(order);
            notificationService.sendNotification(order.getId(), "Order failed due to out of stock");
            return order;
        }

        try {
            paymentService.processPayment(order.getId(), total);
            
            // Confirm stock
            for (OrderItem item : order.getItems()) {
                inventoryService.confirmStock(item.getProductId(), item.getQuantity());
            }
            order.setStatus("COMPLETED");
            orderRepository.save(order);
            notificationService.sendNotification(order.getId(), "Order completed successfully");
        } catch (Exception e) {
            // Rollback inventory
            for (OrderItem item : order.getItems()) {
                inventoryService.releaseStock(item.getProductId(), item.getQuantity());
            }
            order.setStatus("FAILED");
            orderRepository.save(order);
            notificationService.sendNotification(order.getId(), "Order failed due to payment failure");
        }
        
        return order;
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
