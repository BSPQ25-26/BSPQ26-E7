package com.lablend.backend.repository;

import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Rollback(false)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    public void setUp() {
        user = new User();
        user.setName("Jorge");
        user.setEmail("jorge@uni.com");
        user.setPassword("password123");
        user.setRole(UserRole.USER);
    }

    @Test
    public void testSaveUser() {
        User savedUser = userRepository.save(user);
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isGreaterThan(0);
        assertThat(savedUser.getName()).isEqualTo("Jorge");
        assertThat(savedUser.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    public void testFindUserById() {
        User savedUser = userRepository.save(user);
        User foundUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo("jorge@uni.com");
    }

    @Test
    public void testFindUserByName() {
        // Probamos el método personalizado findByName()
        userRepository.save(user);
        User foundUser = userRepository.findByName("Jorge").orElse(null);
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("Jorge");
    }

    @Test
    public void testUpdateUser() {
        User savedUser = userRepository.save(user);
        savedUser.setRole(UserRole.ADMIN);
        User updatedUser = userRepository.save(savedUser);
        assertThat(updatedUser.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    public void testDeleteUser() {
        User savedUser = userRepository.save(user);
        userRepository.delete(savedUser);
        User deletedUser = userRepository.findById(savedUser.getId()).orElse(null);
        assertThat(deletedUser).isNull();
    }
}
