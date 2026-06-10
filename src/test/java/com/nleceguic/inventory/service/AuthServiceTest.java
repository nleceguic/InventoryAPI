package com.nleceguic.inventory.service;

import com.nleceguic.inventory.dto.AuthResponse;
import com.nleceguic.inventory.dto.LoginRequest;
import com.nleceguic.inventory.dto.RegisterRequest;
import com.nleceguic.inventory.model.Role;
import com.nleceguic.inventory.model.User;
import com.nleceguic.inventory.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    @Test
    void register_WithExistingEmail_ThrowsException() {
        when(userRepository.findByEmail("taken@test.com")).thenReturn(Optional.of(new User()));

        RegisterRequest request = new RegisterRequest();
        request.setEmail("taken@test.com");
        request.setUsername("someone");
        request.setPassword("secret123");

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> authService.register(request));
        assertTrue(ex.getMessage().contains("already exists"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_WithValidData_ReturnsAuthResponse() {
        when(userRepository.findByEmail("new@test.com")).thenReturn(Optional.empty());
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("pass1234")).thenReturn("$2a$hashed");

        User saved = new User();
        saved.setId(1L);
        saved.setUsername("newuser");
        saved.setEmail("new@test.com");
        saved.setPassword("$2a$hashed");
        saved.setRole(Role.USER);
        when(userRepository.save(any(User.class))).thenReturn(saved);
        when(jwtService.generateToken(any())).thenReturn("mock-jwt-token");

        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@test.com");
        request.setUsername("newuser");
        request.setPassword("pass1234");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock-jwt-token", response.getToken());
        assertEquals("newuser", response.getUsername());
        assertEquals("new@test.com", response.getEmail());
        assertEquals("USER", response.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_WithInvalidCredentials_ThrowsException() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        LoginRequest request = new LoginRequest();
        request.setEmail("user@test.com");
        request.setPassword("wrongpassword");

        assertThrows(BadCredentialsException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any());
    }
}
