package com.bit41.monolith.controller;

import com.bit41.monolith.model.Order;
import com.bit41.monolith.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public ResponseEntity<Order> createOrder(
            @Valid @RequestBody Order order,
            Authentication authentication) {
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        
        if (!"CUSTOMER".equals(role) && !"ADMIN".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        if ("CUSTOMER".equals(role)) {
            order.setCustomerId(userId);
        }
        
        if (order.getItems() == null || order.getItems().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        Order createdOrder = orderService.createOrder(order);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOrder);
    }

    @GetMapping
    public ResponseEntity<List<Order>> getOrders(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        String role = authentication.getAuthorities().iterator().next().getAuthority();
        UUID userId = UUID.fromString((String) authentication.getPrincipal());
        
        if ("ADMIN".equals(role)) {
            return ResponseEntity.ok(orderService.getAllOrders());
        } else if ("CUSTOMER".equals(role)) {
            return ResponseEntity.ok(orderService.getOrdersByCustomerId(userId));
        }
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
