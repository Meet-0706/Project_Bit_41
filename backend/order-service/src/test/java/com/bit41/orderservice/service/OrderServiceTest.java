package com.bit41.orderservice.service;

import com.bit41.orderservice.model.Order;
import com.bit41.orderservice.model.OrderItem;
import com.bit41.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCreateOrder() {
        Order order = new Order();
        order.setCustomerId(UUID.randomUUID());

        OrderItem item1 = new OrderItem();
        item1.setProductId(UUID.randomUUID());
        item1.setQuantity(2);
        item1.setPrice(new BigDecimal("10.00"));
        order.addItem(item1);

        OrderItem item2 = new OrderItem();
        item2.setProductId(UUID.randomUUID());
        item2.setQuantity(1);
        item2.setPrice(new BigDecimal("15.50"));
        order.addItem(item2);

        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArguments()[0]);

        Order createdOrder = orderService.createOrder(order);

        assertNotNull(createdOrder);
        assertEquals("PENDING", createdOrder.getStatus());
        assertEquals(new BigDecimal("35.50"), createdOrder.getTotalAmount());
        assertEquals(2, createdOrder.getItems().size());
        assertEquals(createdOrder, createdOrder.getItems().get(0).getOrder());

        verify(orderRepository).save(order);
    }
}
