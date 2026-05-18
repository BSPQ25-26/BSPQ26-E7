package com.lablend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import com.lablend.backend.entity.User;
import java.util.Optional;
import com.lablend.backend.entity.UserStatus;

/**
 * Repository interface for managing {@link User} entities.
 * @version 1.0
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByName(String name);
    List<User> findByStatus(UserStatus status);
}