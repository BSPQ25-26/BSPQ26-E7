package com.lablend.backend.service;

import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setName("Maddi");
        user.setEmail("maddi@uni.com");
        user.setRole(UserRole.USER);
    }

    @Test
    public void testCreateUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);
        User createdUser = userService.createUser(user);
        assertNotNull(createdUser);
        assertEquals("Maddi", createdUser.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(user));
        List<User> userList = userService.getAllUsers();
        assertEquals(1, userList.size());
        assertEquals("Maddi", userList.get(0).getName());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void testGetUserById() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        Optional<User> foundUser = userService.getUserById(1L);
        assertTrue(foundUser.isPresent());
        assertEquals("Maddi", foundUser.get().getName());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    public void testGetUserById_NullId() {
        assertThrows(IllegalArgumentException.class, () -> {
            userService.getUserById(null);
        });
    }

    @Test
    public void testUpdateUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        User updatedUser = userService.updateUser(1L, user);
        assertNotNull(updatedUser);
        assertEquals("Maddi", updatedUser.getName());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    public void testUpdateUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(99L, user);
        });
    }

    @Test
    public void testDeleteUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userRepository).delete(user);
        userService.deleteUser(1L);
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    public void testDeleteUser_NotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            userService.deleteUser(99L);
        });
    }
}