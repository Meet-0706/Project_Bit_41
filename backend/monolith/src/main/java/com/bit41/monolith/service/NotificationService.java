package com.bit41.monolith.service;

import com.bit41.monolith.model.Notification;
import com.bit41.monolith.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public Notification sendNotification(UUID orderId, String type) {
        Notification notification = new Notification();
        notification.setOrderId(orderId);
        notification.setType(type);
        notification.setStatus("SENT");
        
        log.info("Simulating sending {} email for Order: {}", type, orderId);

        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }
}
