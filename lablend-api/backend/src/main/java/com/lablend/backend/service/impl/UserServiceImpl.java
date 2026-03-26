package com.lablend.backend.service.impl;

import com.lablend.backend.entity.User;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

     /**
     * Constructor-based dependency injection for the user repository.
     * @param userRepository The persistence repository for user data.
     */
    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Persists a new user record into the database.
     * @param user The user entity to be created.
     * @return The saved user instance with its generated ID.
     */
    @Override
    public User createUser(User user) {
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

}