package com.bit41.paymentservice.service;

import com.bit41.paymentservice.dto.SagaEvent;
import com.bit41.paymentservice.model.ProcessedEvent;
import com.bit41.paymentservice.model.Transaction;
import com.bit41.paymentservice.repository.ProcessedEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class PaymentEventListener {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventListener.class);
    private final PaymentService paymentService;
    private final ProcessedEventRepository processedEventRepository;
    private final RabbitTemplate rabbitTemplate;

    public PaymentEventListener(PaymentService paymentService, ProcessedEventRepository processedEventRepository, RabbitTemplate rabbitTemplate) {
        this.paymentService = paymentService;
        this.processedEventRepository = processedEventRepository;
        this.rabbitTemplate = rabbitTemplate;
    }

    @RabbitListener(queues = "payment.queue")
    @Transactional
    public void handlePaymentEvent(SagaEvent event) {
        if (processedEventRepository.existsById(event.getEventId())) {
            log.info("Duplicate event ignored: {}", event.getEventId());
            return;
        }

        log.info("Received event {} for order {}", event.getEventType(), event.getOrderId());
        
        if ("InventoryReserved".equals(event.getEventType())) {
            // Need to convert Double/Integer to BigDecimal from payload safely
            Object amountObj = event.getPayload().get("amount");
            BigDecimal amount;
            if (amountObj instanceof Double) {
                amount = BigDecimal.valueOf((Double) amountObj);
            } else if (amountObj instanceof Integer) {
                amount = BigDecimal.valueOf((Integer) amountObj);
            } else {
                amount = new BigDecimal(amountObj.toString());
            }

            Transaction tx = paymentService.processPayment(event.getOrderId(), amount);
            
            if ("SUCCESS".equals(tx.getStatus())) {
                rabbitTemplate.convertAndSend("saga.exchange", "order.confirm", 
                    new SagaEvent(event.getSagaId(), event.getOrderId(), "PaymentSucceeded", event.getPayload()));
            } else {
                // Publish to order and inventory to rollback
                rabbitTemplate.convertAndSend("saga.exchange", "order.cancel", 
                    new SagaEvent(event.getSagaId(), event.getOrderId(), "PaymentFailed", event.getPayload()));
                rabbitTemplate.convertAndSend("saga.exchange", "inventory.release", 
                    new SagaEvent(event.getSagaId(), event.getOrderId(), "PaymentFailed", event.getPayload()));
            }
        }

        processedEventRepository.save(new ProcessedEvent(event.getEventId(), event.getSagaId(), event.getEventType()));
    }
}
