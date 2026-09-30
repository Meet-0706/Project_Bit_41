package com.bit41.notificationservice.service;

import com.bit41.notificationservice.model.Notification;
import com.bit41.notificationservice.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendNotification() {
        UUID orderId = UUID.randomUUID();
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);

        Notification result = notificationService.sendNotification(orderId, "ORDER_CONFIRMED");

        assertEquals(orderId, result.getOrderId());
        assertEquals("ORDER_CONFIRMED", result.getType());
        assertEquals("SENT", result.getStatus());
        verify(notificationRepository).save(any(Notification.class));
    }
}
