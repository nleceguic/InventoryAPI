package com.nleceguic.inventory.repository;

import com.nleceguic.inventory.model.Role;
import com.nleceguic.inventory.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = Replace.NONE)
class UserRepositoryTest {

    @Autowired private UserRepository userRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Test
    void save_PersistsAndRetrievesUser() {
        User user = new User();
        user.setUsername("jdoe");
        user.setEmail("jdoe@test.com");
        user.setPassword("hashed-password");
        user.setRole(Role.USER);

        User saved = userRepository.save(user);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());

        Optional<User> found = userRepository.findById(saved.getId());
        assertTrue(found.isPresent());
        assertEquals("jdoe", found.get().getUsername());
        assertEquals(Role.USER, found.get().getRole());
    }

    @Test
    void findByEmail_WithExistingEmail_ReturnsUser() {
        User user = new User();
        user.setUsername("asmith");
        user.setEmail("asmith@test.com");
        user.setPassword("hashed-password");
        user.setRole(Role.ADMIN);
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("asmith@test.com");

        assertTrue(found.isPresent());
        assertEquals("asmith", found.get().getUsername());
    }

    @Test
    void findByUsername_WithExistingUsername_ReturnsUser() {
        User user = new User();
        user.setUsername("bwayne");
        user.setEmail("bwayne@test.com");
        user.setPassword("hashed-password");
        user.setRole(Role.USER);
        userRepository.save(user);

        Optional<User> found = userRepository.findByUsername("bwayne");

        assertTrue(found.isPresent());
        assertEquals("bwayne@test.com", found.get().getEmail());
    }

    @Test
    void findByEmail_WithNoMatch_ReturnsEmpty() {
        Optional<User> found = userRepository.findByEmail("nobody@test.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void insertingInvalidRole_ViolatesRoleCheckConstraint() {
        assertThrows(DataIntegrityViolationException.class, () ->
                jdbcTemplate.update(
                        "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)",
                        "hacker", "hacker@test.com", "hashed-password", "SUPERADMIN"));
    }
}
