package com.nleceguic.inventory.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nleceguic.inventory.dto.CategoryRequest;
import com.nleceguic.inventory.dto.CategoryResponse;
import com.nleceguic.inventory.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CategoryControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockBean  private CategoryService categoryService;

    @Test
    void getCategories_WithoutToken_Returns401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser
    void createCategory_AsUser_Returns403() throws Exception {
        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createCategory_AsAdmin_Returns201() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Books", "Printed and digital books", LocalDateTime.now());
        when(categoryService.create(any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Books"));
    }

    @Test
    @WithMockUser
    void updateCategory_AsUser_Returns403() throws Exception {
        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateCategory_AsAdmin_Returns200() throws Exception {
        CategoryResponse response = new CategoryResponse(1L, "Books", "Updated description", LocalDateTime.now());
        when(categoryService.update(anyLong(), any(CategoryRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/categories/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated description"));
    }

    @Test
    @WithMockUser
    void deleteCategory_AsUser_Returns403() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteCategory_AsAdmin_Returns204() throws Exception {
        mockMvc.perform(delete("/api/categories/1"))
                .andExpect(status().isNoContent());
    }

    private CategoryRequest buildRequest() {
        CategoryRequest request = new CategoryRequest();
        request.setName("Books");
        request.setDescription("Printed and digital books");
        return request;
    }
}
