package com.nleceguic.inventory.repository;

import com.nleceguic.inventory.model.Category;
import com.nleceguic.inventory.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class ProductRepositoryTest {

    @Autowired private ProductRepository productRepository;
    @Autowired private TestEntityManager entityManager;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setName("Test Category " + System.nanoTime());
        category.setDescription("Category created for ProductRepositoryTest");
        entityManager.persistAndFlush(category);
    }

    @Test
    void save_PersistsAndRetrievesProduct() {
        Product saved = productRepository.save(buildProduct("SKU-100", true));

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
        assertNotNull(saved.getUpdatedAt());

        Optional<Product> found = productRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("SKU-100", found.get().getSku());
    }

    @Test
    void findBySku_WithExistingSku_ReturnsProduct() {
        productRepository.save(buildProduct("SKU-200", true));

        Optional<Product> found = productRepository.findBySku("SKU-200");

        assertTrue(found.isPresent());
        assertEquals(category.getId(), found.get().getCategory().getId());
    }

    @Test
    void findBySku_WithNoMatch_ReturnsEmpty() {
        Optional<Product> found = productRepository.findBySku("DOES-NOT-EXIST");

        assertTrue(found.isEmpty());
    }

    @Test
    void existsBySku_ReflectsPresenceOfSku() {
        productRepository.save(buildProduct("SKU-300", true));

        assertTrue(productRepository.existsBySku("SKU-300"));
        assertFalse(productRepository.existsBySku("SKU-DOES-NOT-EXIST"));
    }

    @Test
    void findByActiveTrue_OnlyReturnsActiveProducts() {
        productRepository.save(buildProduct("SKU-400", true));
        productRepository.save(buildProduct("SKU-500", false));

        List<Product> active = productRepository.findByActiveTrue();

        assertTrue(active.stream().anyMatch(p -> p.getSku().equals("SKU-400")));
        assertTrue(active.stream().noneMatch(p -> p.getSku().equals("SKU-500")));
    }

    private Product buildProduct(String sku, boolean active) {
        Product product = new Product();
        product.setName("Test Product " + sku);
        product.setPrice(BigDecimal.valueOf(19.99));
        product.setStock(5);
        product.setSku(sku);
        product.setActive(active);
        product.setCategory(category);
        return product;
    }
}
