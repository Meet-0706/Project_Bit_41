package com.bit41.monolith.controller;

import com.bit41.monolith.model.Stock;
import com.bit41.monolith.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping
    public List<Stock> getAllStock() {
        return inventoryService.getAllStock();
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Stock> getStock(@PathVariable UUID productId) {
        return inventoryService.getStock(productId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{productId}")
    public ResponseEntity<Stock> addStock(@PathVariable UUID productId, @RequestBody Map<String, Integer> request) {
        Integer quantity = request.get("quantity");
        if (quantity == null || quantity <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Stock updatedStock = inventoryService.addStock(productId, quantity);
        return ResponseEntity.ok(updatedStock);
    }
}
