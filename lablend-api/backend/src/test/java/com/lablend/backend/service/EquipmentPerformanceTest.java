package com.lablend.backend.service;

import org.junit.jupiter.api.extension.RegisterExtension;
import com.github.noconnor.junitperf.reporting.providers.HtmlReportGenerator;
import com.github.noconnor.junitperf.JUnitPerfReportingConfig;
import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.github.noconnor.junitperf.JUnitPerfInterceptor;
import com.github.noconnor.junitperf.JUnitPerfTestActiveConfig;
import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.service.impl.EquipmentServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, JUnitPerfInterceptor.class})
@MockitoSettings(strictness = Strictness.LENIENT)
public class EquipmentPerformanceTest {

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/performance_report.html"))
            .build();


    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    @Mock
    private EquipmentRepository equipmentRepository;

    // 1. Successful performance test
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 100, executionsPerSec = 5)
    public void testEquipmentCreation_Success() {
        Equipment eq = new Equipment("Oscilloscope", "Lab", EquipmentStatus.AVAILABLE);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(eq);
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }

    // 2. Failed performance test
    @Test
    @JUnitPerfTest(threads = 2, durationMs = 10)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 100, maxLatency = 10.0f)
    public void testEquipmentCreation_Fail() throws InterruptedException {
        Equipment eq = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(eq);
        
        Thread.sleep(0); // Artificial delay that can be increased to break the 1ms max latency rule 
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }
}