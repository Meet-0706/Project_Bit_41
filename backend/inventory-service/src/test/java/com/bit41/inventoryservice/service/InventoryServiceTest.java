package com.bit41.inventoryservice.service;

import com.bit41.inventoryservice.model.Stock;
import com.bit41.inventoryservice.repository.StockRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class InventoryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @InjectMocks
    private InventoryService inventoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAddStock_NewProduct() {
        UUID productId = UUID.randomUUID();
        when(stockRepository.findById(productId)).thenReturn(Optional.empty());
        when(stockRepository.save(any(Stock.class))).thenAnswer(i -> i.getArguments()[0]);

        Stock result = inventoryService.addStock(productId, 10);
        
        assertEquals(10, result.getAvailableQuantity());
        assertEquals(0, result.getReservedQuantity());
        verify(stockRepository).save(any(Stock.class));
    }

    @Test
    void testReserveStock_Success() {
        UUID productId = UUID.randomUUID();
        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setAvailableQuantity(15);
        stock.setReservedQuantity(0);

        when(stockRepository.findById(productId)).thenReturn(Optional.of(stock));

        boolean reserved = inventoryService.reserveStock(productId, 5);
        
        assertTrue(reserved);
        assertEquals(10, stock.getAvailableQuantity());
        assertEquals(5, stock.getReservedQuantity());
        verify(stockRepository).save(stock);
    }

    @Test
    void testReserveStock_InsufficientQuantity() {
        UUID productId = UUID.randomUUID();
        Stock stock = new Stock();
        stock.setProductId(productId);
        stock.setAvailableQuantity(3);
        stock.setReservedQuantity(0);

        when(stockRepository.findById(productId)).thenReturn(Optional.of(stock));

        boolean reserved = inventoryService.reserveStock(productId, 5);
        
        assertFalse(reserved);
        assertEquals(3, stock.getAvailableQuantity());
        assertEquals(0, stock.getReservedQuantity());
        verify(stockRepository, never()).save(any(Stock.class));
    }
}
