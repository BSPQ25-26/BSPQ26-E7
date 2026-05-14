package com.lablend.backend.service;

import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.controller.UserController;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;
import java.util.UUID;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(JUnitPerfInterceptor.class)
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb-perf",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
public class UserPerformanceTest {

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/user_performance_report.html"))
            .build();

    @Autowired
    private UserService userService;

    @Autowired
    private UserController userController;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        try {
            userRepository.deleteAll();
        } catch (Exception e) {
            // Ignore concurrent deletion issues
        }
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 10, meanLatency = 2000.0f, maxLatency = 5000.0f)
    public void testUserCreation_Throughput() {
        String uniqueEmail = "jorge." + UUID.randomUUID().toString() + "@deusto.com";
        User user = new User("Jorge", uniqueEmail, "password", UserRole.USER);
        
        User created = userService.createUser(user);
        assertNotNull(created);
    }

    @Test
    @JUnitPerfTest(threads = 20, durationMs = 2000, maxExecutionsPerSecond = 200)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 10, meanLatency = 2000.0f, maxLatency = 5000.0f)
    public void testGetUserById_Throughput() {
        String uniqueEmail = "jorge." + UUID.randomUUID().toString() + "@deusto.com";
        User user = new User("Jorge", uniqueEmail, "password", UserRole.USER);
        User created = userRepository.save(user);

        Optional<User> found = userService.getUserById(created.getId());
        assertNotNull(found.orElse(null));
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 10, meanLatency = 2000.0f, maxLatency = 5000.0f)
    public void testUserController_CreateUser_Throughput() {
        String uniqueEmail = "jorge." + UUID.randomUUID().toString() + "@deusto.com";
        User user = new User("Jorge", uniqueEmail, "password", UserRole.USER);
        
        ResponseEntity<?> response = userController.createUser(user);
        assertNotNull(response);
    }

    @Test
    @JUnitPerfTest(threads = 20, durationMs = 2000, maxExecutionsPerSecond = 200)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 10, meanLatency = 2000.0f, maxLatency = 5000.0f)
    public void testUserController_GetUserById_Throughput() {
        String uniqueEmail = "jorge." + UUID.randomUUID().toString() + "@deusto.com";
        User user = new User("Jorge", uniqueEmail, "password", UserRole.USER);
        User created = userRepository.save(user);

        ResponseEntity<User> response = userController.getUserById(created.getId());
        assertNotNull(response);
    }
}
