package com.bit41.inventoryservice.service;

import com.bit41.inventoryservice.model.Stock;
import com.bit41.inventoryservice.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class InventoryService {

    private final StockRepository stockRepository;

    public InventoryService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    public List<Stock> getAllStock() {
        return stockRepository.findAll();
    }

    public Optional<Stock> getStock(UUID productId) {
        return stockRepository.findById(productId);
    }

    @Transactional
    public Stock addStock(UUID productId, int quantity) {
        Stock stock = stockRepository.findById(productId).orElseGet(() -> {
            Stock newStock = new Stock();
            newStock.setProductId(productId);
            return newStock;
        });
        
        stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
        return stockRepository.save(stock);
    }

    // Reservation logic will be hooked up to RabbitMQ listeners in Phase 4
    @Transactional
    public boolean reserveStock(UUID productId, int quantity) {
        Optional<Stock> stockOpt = stockRepository.findById(productId);
        if (stockOpt.isEmpty()) return false;
        
        Stock stock = stockOpt.get();
        if (stock.getAvailableQuantity() >= quantity) {
            stock.setAvailableQuantity(stock.getAvailableQuantity() - quantity);
            stock.setReservedQuantity(stock.getReservedQuantity() + quantity);
            stockRepository.save(stock);
            return true;
        }
        return false;
    }
    
    @Transactional
    public void releaseStock(UUID productId, int quantity) {
        stockRepository.findById(productId).ifPresent(stock -> {
            stock.setReservedQuantity(Math.max(0, stock.getReservedQuantity() - quantity));
            stock.setAvailableQuantity(stock.getAvailableQuantity() + quantity);
            stockRepository.save(stock);
        });
    }
    
    @Transactional
    public void confirmStock(UUID productId, int quantity) {
        stockRepository.findById(productId).ifPresent(stock -> {
            stock.setReservedQuantity(Math.max(0, stock.getReservedQuantity() - quantity));
            stockRepository.save(stock);
        });
    }
}
