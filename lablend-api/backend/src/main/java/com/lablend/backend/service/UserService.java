package com.lablend.backend.service;

import com.lablend.backend.entity.User;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for managing User entities.
 * Provides CRUD operations for user management.
 */
public interface UserService {
    User createUser(User user);
    User updateUser(Long id, User user);
    void deleteUser(Long id);
    Optional<User> getUserById(Long id);
    List<User> getAllUsers();
}