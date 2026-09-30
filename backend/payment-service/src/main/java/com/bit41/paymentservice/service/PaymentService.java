package com.bit41.paymentservice.service;

import com.bit41.paymentservice.model.Transaction;
import com.bit41.paymentservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {

    private final TransactionRepository transactionRepository;
    
    // We make it mutable to allow admin to change it on the fly
    private double currentFailureRate;

    public PaymentService(TransactionRepository transactionRepository, 
                          @Value("${payment.failure-rate:0.2}") double initialFailureRate) {
        this.transactionRepository = transactionRepository;
        this.currentFailureRate = initialFailureRate;
    }

    public Transaction processPayment(UUID orderId, BigDecimal amount) {
        Transaction tx = new Transaction();
        tx.setOrderId(orderId);
        tx.setAmount(amount);

        double randomValue = ThreadLocalRandom.current().nextDouble();
        if (randomValue < currentFailureRate) {
            tx.setStatus("FAILED");
        } else {
            tx.setStatus("SUCCESS");
        }

        return transactionRepository.save(tx);
    }

    public void setFailureRate(double rate) {
        if (rate >= 0.0 && rate <= 1.0) {
            this.currentFailureRate = rate;
        }
    }

    public double getFailureRate() {
        return this.currentFailureRate;
    }

    public List<Transaction> getAllTransactions() {
        return transactionRepository.findAll();
    }
}
