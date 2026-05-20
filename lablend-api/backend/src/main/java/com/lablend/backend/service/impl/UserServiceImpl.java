package com.lablend.backend.service.impl;

import com.lablend.backend.entity.User;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.entity.UserStatus;


import java.util.List;
import java.util.Optional;
/**
 * Implementation of the UserService interface.
 * Handle the logic for user management, ensuring data consistency
 * through validation and exception handling.
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

     /**
     * Constructor-based dependency injection for the user repository.
     * @param userRepository The persistence repository for user data.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Persists a new user record into the database.
     * @param user The user entity to be created.
     * @return The saved user instance with its generated ID.
     */
    @Override
    public User createUser(User user) {
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return userRepository.save(user);
    }

    /**
     * Updates an existing user record by its unique identifier.
     * Updates the user's name, email, and role atomically.
     * @param id The ID of the user to update.
     * @param userDetails The user object containing the new data.
     * @return The updated and persisted user entity.
     * @throws IllegalArgumentException if the provided ID is null.
     * @throws RuntimeException if no user is found with the given ID.
     */
    @Override
    public User updateUser(Long id, User userDetails) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        user.setName(userDetails.getName());
        user.setEmail(userDetails.getEmail());
        user.setRole(userDetails.getRole());
        
        if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
        }

        return userRepository.save(user);
    }


    /**
     * Removes a user record from the system.
     * @param id The ID of the user to delete.
     * @throws IllegalArgumentException if the provided ID is null.
     * @throws RuntimeException if no user is found with the given ID.
     */
    @Override
    public void deleteUser(Long id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        userRepository.delete(user);
    }
    /**
     * Retrieves a user record by its unique identifier.
     * @param id The ID of the user to find.
     * @return An Optional containing the user if found, or empty if not.
     * @throws IllegalArgumentException if the provided ID is null.
     */
    @Override
    public Optional<User> getUserById(Long id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return userRepository.findById(id);
    }
    /**
     * Retrieves all user records currently stored in the system.
     * @return A list of all user entities.
     */
    @Override
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }


    /**
     * Blocks a user by setting their status to BLOCKED.
     *
     * <p>Administrators cannot be blocked.</p>
     *
     * @param id the ID of the user to block
     * @throws RuntimeException if no user exists with the given ID
     * @throws IllegalStateException if the user is an administrator
     */
    @Override
    public void blockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        if (user.getRole() == UserRole.ADMIN) {
            throw new IllegalStateException("Administrators cannot be blocked");
        }
        user.setStatus(UserStatus.BLOCKED);
        userRepository.save(user);
    }

    /**
     * Unblocks a user by setting their status to ACTIVE.
     *
     * @param id the ID of the user to unblock
     * @throws RuntimeException if no user exists with the given ID
     */
    @Override
    public void unblockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
    /**
     * Retrieves all users whose status is BLOCKED.
     *
     * @return a list of blocked users
     */
    @Override
    public List<User> getBlockedUsers() {
        return userRepository.findByStatus(UserStatus.BLOCKED);
    }
    /**
     * Checks whether a user is currently blocked.
     *
     * @param id the ID of the user to check
     * @return true if the user is blocked, false otherwise
     * @throws RuntimeException if no user exists with the given ID
     */
    @Override
    public boolean isUserBlocked(Long id) {
        return userRepository.findById(id)
                .map(u -> u.getStatus() == UserStatus.BLOCKED)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

}