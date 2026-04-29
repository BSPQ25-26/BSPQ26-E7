package com.lablend.backend.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    @Test
    void testUserDefaultConstructorAndSetters() {
        User user = new User();
        user.setId(1L);
        user.setName("Jorge");
        user.setEmail("jorge@uni.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);

        assertEquals(1L, user.getId());
        assertEquals("Jorge", user.getName());
        assertEquals("jorge@uni.com", user.getEmail());
        assertEquals("password123", user.getPassword());
        assertEquals(UserRole.USER, user.getRole());
    }

    @Test
    void testUserParameterizedConstructor() {
        User user = new User("Maddi", "maddi@uni.com", "pass456", UserRole.ADMIN);

        // El ID será null inicialmente porque no se pasa en el constructor
        assertNull(user.getId());
        assertEquals("Maddi", user.getName());
        assertEquals("maddi@uni.com", user.getEmail());
        assertEquals("pass456", user.getPassword());
        assertEquals(UserRole.ADMIN, user.getRole());
    }
}
