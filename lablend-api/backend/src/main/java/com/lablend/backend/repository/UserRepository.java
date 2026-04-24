package com.lablend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lablend.backend.entity.User;
import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * @version 1.0
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
}