package com.nleceguic.inventory.service;

import com.nleceguic.inventory.dto.ProductRequest;
import com.nleceguic.inventory.dto.ProductResponse;
import com.nleceguic.inventory.dto.StockAdjustmentRequest;
import com.nleceguic.inventory.model.Category;
import com.nleceguic.inventory.model.Product;
import com.nleceguic.inventory.model.Role;
import com.nleceguic.inventory.model.User;
import com.nleceguic.inventory.repository.CategoryRepository;
import com.nleceguic.inventory.repository.ProductRepository;
import com.nleceguic.inventory.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ProductService productService;

    private Category category;
    private User user;
    private Product product;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setEmail("admin@test.com");
        user.setRole(Role.ADMIN);

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setSku("LAP-001");
        product.setPrice(BigDecimal.valueOf(999.99));
        product.setStock(10);
        product.setActive(true);
        product.setCategory(category);
        product.setCreatedBy(user);
    }

    @Test
    void createProduct_WithDuplicateSku_ThrowsException() {
        when(productRepository.existsBySku("LAP-001")).thenReturn(true);

        ProductRequest request = buildRequest("Laptop", "LAP-001", BigDecimal.valueOf(999), 1L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.create(request, "admin@test.com"));
        assertTrue(ex.getMessage().contains("already exists"));
    }

    @Test
    void createProduct_WithValidData_ReturnsProductResponse() {
        ProductRequest request = buildRequest("New Laptop", "LAP-002", BigDecimal.valueOf(899), 1L);
        request.setStock(5);

        Product saved = new Product();
        saved.setId(2L);
        saved.setName("New Laptop");
        saved.setSku("LAP-002");
        saved.setPrice(BigDecimal.valueOf(899));
        saved.setStock(5);
        saved.setActive(true);
        saved.setCategory(category);

        when(productRepository.existsBySku("LAP-002")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));
        when(productRepository.save(any(Product.class))).thenReturn(saved);

        ProductResponse response = productService.create(request, "admin@test.com");

        assertNotNull(response);
        assertEquals(2L, response.getId());
        assertEquals("New Laptop", response.getName());
        assertEquals("LAP-002", response.getSku());
        assertEquals("Electronics", response.getCategoryName());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void adjustStock_WithNegativeResult_ThrowsException() {
        product.setStock(5);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(-10);

        assertThrows(IllegalArgumentException.class,
                () -> productService.adjustStock(1L, request));
        verify(productRepository, never()).save(any());
    }

    @Test
    void adjustStock_WithValidQuantity_UpdatesStock() {
        product.setStock(10);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product updated = new Product();
        updated.setId(1L);
        updated.setName(product.getName());
        updated.setSku(product.getSku());
        updated.setPrice(product.getPrice());
        updated.setStock(13);
        updated.setActive(true);
        updated.setCategory(category);
        when(productRepository.save(any(Product.class))).thenReturn(updated);

        StockAdjustmentRequest request = new StockAdjustmentRequest();
        request.setQuantity(3);

        ProductResponse response = productService.adjustStock(1L, request);

        assertEquals(13, response.getStock());
        verify(productRepository).save(argThat(p -> p.getStock() == 13));
    }

    @Test
    void deactivateProduct_SetsActiveFalse() {
        product.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Product deactivated = new Product();
        deactivated.setId(1L);
        deactivated.setName(product.getName());
        deactivated.setSku(product.getSku());
        deactivated.setPrice(product.getPrice());
        deactivated.setStock(product.getStock());
        deactivated.setActive(false);
        deactivated.setCategory(category);
        when(productRepository.save(any(Product.class))).thenReturn(deactivated);

        ProductResponse response = productService.deactivate(1L);

        assertFalse(response.getActive());
        verify(productRepository).save(argThat(p -> Boolean.FALSE.equals(p.getActive())));
    }

    @Test
    void findById_WithInvalidId_ThrowsException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> productService.findById(999L));
        assertTrue(ex.getMessage().toLowerCase().contains("not found"));
    }

    private ProductRequest buildRequest(String name, String sku, BigDecimal price, Long categoryId) {
        ProductRequest r = new ProductRequest();
        r.setName(name);
        r.setSku(sku);
        r.setPrice(price);
        r.setCategoryId(categoryId);
        return r;
    }
}
