package com.lablend.backend.service;

import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.controller.LoanController;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
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
public class LoanPerformanceTest {

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/loan_performance_report.html"))
            .build();

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanController loanController;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void tearDown() {
        try {
            loanRepository.deleteAll();
            equipmentRepository.deleteAll();
            userRepository.deleteAll();
        } catch (Exception e) {
            // Ignore concurrent deletion issues
        }
    }
    
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 50)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 10, meanLatency = 2000.0f, maxLatency = 5000.0f)
    public void testLoanCreation_Throughput() {
        String uniqueEmail = "jorge." + UUID.randomUUID().toString() + "@deusto.com";
        User user = new User("Jorge", uniqueEmail, "password", UserRole.USER);
        User savedUser = userRepository.save(user);

        Equipment equipment = new Equipment("Microscope", "Optical", EquipmentStatus.AVAILABLE);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Loan loan = new Loan(savedUser.getId(), savedEquipment.getId(), LocalDateTime.now(), LoanStatus.ACTIVE);

        Loan created = loanService.createLoan(loan);
        assertNotNull(created);
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 50)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 10, meanLatency = 2000.0f, maxLatency = 5000.0f)
    public void testLoanController_CreateLoan_Throughput() {
        String uniqueEmail = "jorge.controller." + UUID.randomUUID().toString() + "@deusto.com";
        User user = new User("Jorge", uniqueEmail, "password", UserRole.USER);
        User savedUser = userRepository.save(user);

        Equipment equipment = new Equipment("Microscope Controller", "Optical", EquipmentStatus.AVAILABLE);
        Equipment savedEquipment = equipmentRepository.save(equipment);

        Loan loan = new Loan(savedUser.getId(), savedEquipment.getId(), LocalDateTime.now(), LoanStatus.ACTIVE);

        ResponseEntity<?> response = loanController.createLoan(loan);
        assertNotNull(response);
    }
}
