package com.lablend.backend.controller;

import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean; 
import com.lablend.backend.auth.filter.JwtAuthenticationFilter;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.lablend.backend.entity.UserStatus;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGetAllUsers() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Maddi");
        user.setEmail("maddi@uni.com");
        user.setRole(UserRole.USER);

        when(userService.getAllUsers()).thenReturn(List.of(user));

        mockMvc.perform(get("/api/users"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Maddi"))
                .andExpect(jsonPath("$[0].email").value("maddi@uni.com"))
                .andExpect(jsonPath("$[0].role").value("USER"));
    }

    @Test
    void testGetUserById() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Maddi");
        user.setEmail("maddi@uni.com");
        user.setRole(UserRole.USER);

        when(userService.getUserById(1L)).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maddi"))
                .andExpect(jsonPath("$.email").value("maddi@uni.com"));
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        when(userService.getUserById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateUser() throws Exception {
        User user = new User();
        user.setId(1L);
        user.setName("Maddi");
        user.setEmail("maddi@uni.com");
        user.setRole(UserRole.USER);

        when(userService.createUser(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Maddi",
                            "email": "maddi@uni.com",
                            "role": "USER"
                        }
                        """))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maddi"))
                .andExpect(jsonPath("$.email").value("maddi@uni.com"));
    }

    @Test
    void testCreateUser_Fail() throws Exception {
        when(userService.createUser(any(User.class)))
                .thenThrow(new RuntimeException("Email already exists"));

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Maddi",
                            "email": "maddi@uni.com",
                            "role": "USER"
                        }
                        """))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Email already exists"));
    }

    @Test
    void testUpdateUser() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setName("Maddi Updated");
        updatedUser.setEmail("maddi@uni.com");
        updatedUser.setRole(UserRole.ADMIN);

        when(userService.updateUser(any(Long.class), any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "name": "Maddi Updated",
                            "email": "maddi@uni.com",
                            "role": "ADMIN"
                        }
                        """))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Maddi Updated"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testDeleteUser() throws Exception {
        mockMvc.perform(delete("/api/users/1"))
                .andExpect(status().isNoContent());
    }


    /**Blocked status tests */
    @Test
    void testBlockUser_Success() throws Exception {
        doNothing().when(userService).blockUser(1L);

        mockMvc.perform(put("/api/users/1/block"))
                .andExpect(status().isOk());
    }

    @Test
    void testBlockUser_AdminConflict() throws Exception {
        doThrow(new IllegalStateException("Administrators cannot be blocked"))
                .when(userService).blockUser(1L);

        mockMvc.perform(put("/api/users/1/block"))
                .andExpect(status().isConflict())
                .andExpect(content().string("Administrators cannot be blocked"));
    }

    @Test
    void testBlockUser_NotFound() throws Exception {
        doThrow(new RuntimeException("User not found"))
                .when(userService).blockUser(99L);

        mockMvc.perform(put("/api/users/99/block"))
                .andExpect(status().isNotFound());
    }

   @Test
    void testUnblockUser_Success() throws Exception {
        User regularUser = new User();
        regularUser.setId(1L);
        regularUser.setRequiresManualReview(false);

        when(userService.getUserById(1L)).thenReturn(Optional.of(regularUser));
        doNothing().when(userService).unblockUser(1L);

        mockMvc.perform(put("/api/users/1/unblock"))
                .andExpect(status().isOk());
    }

    @Test
    void testUnblockUser_NotFound() throws Exception {
        doThrow(new RuntimeException("User not found"))
                .when(userService).unblockUser(99L);

        mockMvc.perform(put("/api/users/99/unblock"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetBlockedUsers() throws Exception {
        User blocked = new User();
        blocked.setId(2L);
        blocked.setName("Blocked User");
        blocked.setEmail("blocked@uni.com");
        blocked.setRole(UserRole.USER);
        blocked.setStatus(UserStatus.BLOCKED);

        when(userService.getBlockedUsers()).thenReturn(List.of(blocked));

        mockMvc.perform(get("/api/users/blocked"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Blocked User"))
                .andExpect(jsonPath("$[0].status").value("BLOCKED"));
    }

    @Test
    void testUnblockUser_Forbidden_WhenManualReviewIsRequired() throws Exception {
        User criticalUser = new User();
        criticalUser.setId(1L);
        criticalUser.setName("John Recidivist");
        criticalUser.setEmail("john@uni.com");
        criticalUser.setRole(UserRole.USER);
        criticalUser.setStatus(UserStatus.BLOCKED);
        criticalUser.setPenaltyCount(3);
        criticalUser.setRequiresManualReview(true);

        when(userService.getUserById(1L)).thenReturn(Optional.of(criticalUser));

        mockMvc.perform(put("/api/users/1/unblock"))
                .andDo(print())
                // Verificamos que devuelva 403 Forbidden debido al bloqueo por reincidencia
                .andExpect(status().isForbidden())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Account requires a manual administrative override review")));
    }
}