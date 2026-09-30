package com.bit41.paymentservice.service;

import com.bit41.paymentservice.model.Transaction;
import com.bit41.paymentservice.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    private PaymentService paymentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testProcessPayment_AlwaysSuccess() {
        paymentService = new PaymentService(transactionRepository, 0.0); // 0% failure rate
        
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction tx = paymentService.processPayment(UUID.randomUUID(), new BigDecimal("100.00"));
        
        assertEquals("SUCCESS", tx.getStatus());
        assertEquals(new BigDecimal("100.00"), tx.getAmount());
    }

    @Test
    void testProcessPayment_AlwaysFail() {
        paymentService = new PaymentService(transactionRepository, 1.0); // 100% failure rate
        
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArguments()[0]);

        Transaction tx = paymentService.processPayment(UUID.randomUUID(), new BigDecimal("100.00"));
        
        assertEquals("FAILED", tx.getStatus());
    }
}
