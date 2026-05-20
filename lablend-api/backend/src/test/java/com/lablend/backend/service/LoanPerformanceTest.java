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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("perf")
@org.junit.jupiter.api.extension.ExtendWith(JUnitPerfInterceptor.class)
public class LoanPerformanceTest {

    private static final AtomicLong SEQUENCE = new AtomicLong();

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/loan_performance_report.html"))
            .build();

    @Autowired
    private LoanService loanService;

    @Autowired
    private LoanRepository loanRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 50)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, meanLatency = 150.0f, maxLatency = 600.0f)
    public void testLoanCreation_Throughput() {
        long suffix = SEQUENCE.incrementAndGet();
        User user = userRepository.save(new User(
                "Jorge-" + suffix,
                "jorge-loan-" + suffix + "@deusto.com",
                "password",
                UserRole.USER));

        Equipment equipment = equipmentRepository.save(new Equipment(
                "Microscope-" + suffix,
                "Optical-" + suffix,
                EquipmentStatus.AVAILABLE));

        Loan loan = new Loan(user.getId(), equipment.getId(), LocalDateTime.now(), LoanStatus.ACTIVE);
        Loan created = loanService.createLoan(loan);
        assertNotNull(created);
                long createdLoanId = created.getId().longValue();
                long equipmentId = equipment.getId().longValue();
                assertNotNull(loanRepository.findById(createdLoanId).orElseThrow());
                assertNotNull(equipmentRepository.findById(equipmentId).orElseThrow());
    }
}
