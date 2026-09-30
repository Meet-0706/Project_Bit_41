package com.bit41.catalogservice.controller;

import com.bit41.catalogservice.model.Product;
import com.bit41.catalogservice.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ProductControllerTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllProducts() {
        Product p1 = new Product();
        p1.setSku("SKU1");
        Product p2 = new Product();
        p2.setSku("SKU2");

        when(productRepository.findAll()).thenReturn(Arrays.asList(p1, p2));

        List<Product> result = productController.getAllProducts();
        assertEquals(2, result.size());
    }

    @Test
    void testCreateProduct_Success() {
        Product p = new Product();
        p.setSku("SKU-123");
        p.setName("Laptop");
        p.setPrice(new BigDecimal("999.99"));

        when(productRepository.findBySku("SKU-123")).thenReturn(Optional.empty());
        when(productRepository.save(any(Product.class))).thenReturn(p);

        ResponseEntity<Product> response = productController.createProduct(p);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Laptop", response.getBody().getName());
    }

    @Test
    void testCreateProduct_Conflict() {
        Product p = new Product();
        p.setSku("SKU-123");

        when(productRepository.findBySku("SKU-123")).thenReturn(Optional.of(p));

        ResponseEntity<Product> response = productController.createProduct(p);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(productRepository, never()).save(any(Product.class));
    }
}
