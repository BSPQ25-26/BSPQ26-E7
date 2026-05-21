package com.lablend.backend.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityRestrictionsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void accessLoans_WithoutToken_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessUsers_WithoutToken_ShouldBeForbidden() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    void blockUser_WithoutToken_ShouldBeForbidden() throws Exception {
        mockMvc.perform(put("/api/users/1/block"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_WithoutToken_ShouldBeForbidden() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isForbidden());
    }
}