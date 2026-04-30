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
import com.lablend.backend.service.impl.UserServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, JUnitPerfInterceptor.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserPerformanceTest {

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/user_performance_report.html"))
            .build();

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

   
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, executionsPerSec = 10, meanLatency = 100.0f, maxLatency = 500.0f)
    public void testUserCreation_Throughput() {
        User user = new User("Jorge", "jorge@deusto.com", "password", UserRole.USER);
        
        synchronized(this) {
            when(userRepository.existsByEmail("jorge@deusto.com")).thenReturn(false);
            when(passwordEncoder.encode(any(String.class))).thenReturn("encoded_pass");
            when(userRepository.save(any(User.class))).thenReturn(user);
        }
        
        User created = userService.createUser(user);
        assertNotNull(created);
    }

   
    @Test
    @JUnitPerfTest(threads = 20, durationMs = 2000, maxExecutionsPerSecond = 200)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, meanLatency = 50.0f, maxLatency = 200.0f)
    public void testGetUserById_Throughput() {
        User user = new User("Jorge", "jorge@deusto.com", "password", UserRole.USER);
        user.setId(1L);

        synchronized(this) {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        }

        Optional<User> found = userService.getUserById(1L);
        assertNotNull(found.orElse(null));
    }
}
