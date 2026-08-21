package com.nleceguic.inventory.repository;

import com.nleceguic.inventory.model.Category;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class CategoryRepositoryTest {

    @Autowired private CategoryRepository categoryRepository;

    @Test
    void save_PersistsAndRetrievesCategory() {
        Category category = new Category();
        category.setName("Books");
        category.setDescription("Printed and digital books");

        Category saved = categoryRepository.save(category);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        Optional<Category> found = categoryRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("Books", found.get().getName());
    }

    @Test
    void findByName_WithExistingName_ReturnsCategory() {
        Category category = new Category();
        category.setName("Garden");
        category.setDescription("Outdoor and garden equipment");
        categoryRepository.save(category);

        Optional<Category> found = categoryRepository.findByName("Garden");

        assertTrue(found.isPresent());
        assertEquals("Outdoor and garden equipment", found.get().getDescription());
    }

    @Test
    void findByName_WithNoMatch_ReturnsEmpty() {
        Optional<Category> found = categoryRepository.findByName("Nonexistent");

        assertTrue(found.isEmpty());
    }
}
