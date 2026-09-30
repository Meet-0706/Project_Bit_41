package com.bit41.notificationservice.service;

import com.bit41.notificationservice.dto.SagaEvent;
import com.bit41.notificationservice.model.ProcessedEvent;
import com.bit41.notificationservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationEventListener {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventListener.class);
    private final NotificationService notificationService;
    private final ProcessedEventRepository processedEventRepository;

    public NotificationEventListener(NotificationService notificationService, ProcessedEventRepository processedEventRepository) {
        this.notificationService = notificationService;
        this.processedEventRepository = processedEventRepository;
    }

    @RabbitListener(queues = "notification.queue")
    @Transactional
    public void handleNotificationEvent(SagaEvent event) {
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        log.info("Received event {} for order {}", event.getEventType(), event.getOrderId());
        
        if ("OrderConfirmed".equals(event.getEventType()) || "OrderCancelled".equals(event.getEventType())) {
            notificationService.sendNotification(event.getOrderId(), event.getEventType());
        }

        processedEventRepository.save(new ProcessedEvent(event.getEventId(), event.getSagaId(), event.getEventType()));
    }
}
