package com.nleceguic.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nleceguic.inventory.dto.ProductRequest;
import com.nleceguic.inventory.dto.ProductResponse;
import com.nleceguic.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProductControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private ProductService productService;

    @Test
    void getProducts_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void getProducts_WithValidToken_Returns200() throws Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser
    void createProduct_WithInvalidBody_Returns400() throws Exception {
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void createProduct_WithValidBody_Returns201() throws Exception {
        ProductRequest request = new ProductRequest();
        request.setName("Test Product");
        request.setPrice(BigDecimal.valueOf(49.99));
        request.setStock(20);
        request.setSku("PROD-001");
        request.setCategoryId(1L);

        ProductResponse response = new ProductResponse();
        response.setId(1L);
        response.setName("Test Product");
        response.setSku("PROD-001");
        response.setPrice(BigDecimal.valueOf(49.99));
        response.setStock(20);
        response.setActive(true);
        response.setCategoryName("Electronics");

        when(productService.create(any(ProductRequest.class), anyString())).thenReturn(response);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("PROD-001"))
                .andExpect(jsonPath("$.categoryName").value("Electronics"));
    }

    @Test
    @WithMockUser
    void adjustStock_BelowZero_Returns400() throws Exception {
        when(productService.adjustStock(anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Stock cannot be negative"));

        mockMvc.perform(patch("/api/products/1/stock")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\": -100}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Stock cannot be negative"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void deleteProduct_AsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteProduct_AsAdmin_Returns204() throws Exception {
        when(productService.deactivate(anyLong())).thenReturn(new ProductResponse());

        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }
}
