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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("perf")
@org.junit.jupiter.api.extension.ExtendWith(JUnitPerfInterceptor.class)
public class UserPerformanceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/user_performance_report.html"))
            .build();

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long seededUserId;

    @BeforeEach
    void setUp() {
        User seededUser = new User(
                "Seed User",
                "seed-user-" + SEQUENCE.incrementAndGet() + "@lablend.local",
                passwordEncoder.encode("password"),
                UserRole.USER);
        seededUser = userRepository.save(seededUser);
        seededUserId = seededUser.getId();
    }

   
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, executionsPerSec = 10, meanLatency = 300.0f, maxLatency = 500.0f)
    public void testUserCreation_Throughput() {
        long suffix = SEQUENCE.incrementAndGet();
        User user = new User(
                "Jorge-" + suffix,
                "jorge-" + suffix + "@deusto.com",
                "password",
                UserRole.USER);

        User created = userService.createUser(user);
        assertNotNull(created);
        long createdId = created.getId().longValue();
        assertNotNull(userRepository.findById(createdId).orElseThrow());
    }

   
    @Test
    @JUnitPerfTest(threads = 20, durationMs = 2000, maxExecutionsPerSecond = 200)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, meanLatency = 50.0f, maxLatency = 200.0f)
    public void testGetUserById_Throughput() {
        Optional<User> found = userService.getUserById(seededUserId);
        assertNotNull(found.orElse(null));
    }
}
