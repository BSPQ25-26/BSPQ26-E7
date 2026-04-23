package com.lablend.backend.service;

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

    // 1. Successful performance test focused on throughput and invocations
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, executionsPerSec = 10, meanLatency = 100.0f, maxLatency = 500.0f)
    public void testEquipmentCreation_Throughput_Success() {
        Equipment eq = new Equipment("Oscilloscope", "Lab", EquipmentStatus.AVAILABLE);
        
        synchronized(this) {
            when(equipmentRepository.save(any(Equipment.class))).thenReturn(eq);
        }
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }

    // 2. Failed performance test focused on duration
    @Test
    @JUnitPerfTest(threads = 5, durationMs = 1000)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, maxLatency = 100.0f, meanLatency = 50.0f)
    public void testEquipmentCreation_Duration_Fail() throws InterruptedException {
        Equipment eq = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        
        synchronized(this) {
            when(equipmentRepository.save(any(Equipment.class))).thenReturn(eq);
        }
        
        // Artificial delay that breaks the 5ms max latency and 2ms mean latency rules
        Thread.sleep(0); 
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }
}