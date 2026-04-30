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
import com.lablend.backend.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, JUnitPerfInterceptor.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanPerformanceTest {

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/loan_performance_report.html"))
            .build();

    @InjectMocks
    private LoanServiceImpl loanService;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;

    
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 50)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, meanLatency = 150.0f, maxLatency = 600.0f)
    public void testLoanCreation_Throughput() {
        User user = new User("Jorge", "jorge@deusto.com", "password", UserRole.USER);
        user.setId(1L);

        Equipment equipment = new Equipment("Microscope", "Optical", EquipmentStatus.AVAILABLE);
        equipment.setId(2L);

        Loan loan = new Loan(1L, 2L, LocalDateTime.now(), LoanStatus.ACTIVE);

        synchronized(this) {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(equipmentRepository.findById(2L)).thenReturn(Optional.of(equipment));
            when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        }

        Loan created = loanService.createLoan(loan);
        assertNotNull(created);
    }
}
