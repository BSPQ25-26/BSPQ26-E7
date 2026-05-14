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
import com.lablend.backend.controller.EquipmentController;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
public class EquipmentPerformanceTest {

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/equipment_performance_report.html"))
            .build();

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentController equipmentController;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @AfterEach
    void tearDown() {
        try {
            equipmentRepository.deleteAll();
        } catch (Exception e) {
            // Ignore concurrent deletion issues
        }
    }

    // 1. Successful performance test focused on throughput and invocations
    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, executionsPerSec = 10, meanLatency = 100.0f, maxLatency = 500.0f)
    public void testEquipmentCreation_Throughput_Success() {
        Equipment eq = new Equipment("Oscilloscope", "Lab", EquipmentStatus.AVAILABLE);
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }

    // 2. Failed performance test focused on duration
    @Test
    // @JUnitPerfTest(threads = 5, durationMs = 60000) to get some time for the profiling
    @JUnitPerfTest(threads = 5, durationMs = 2000)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, maxLatency = 100.0f, meanLatency = 50.0f)
    public void testEquipmentCreation_Duration_Fail() throws InterruptedException {
        Equipment eq = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        
        // Artificial delay that breaks the 5ms max latency and 2ms mean latency rules
        Thread.sleep(1000); 
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, meanLatency = 100.0f, maxLatency = 500.0f)
    public void testEquipmentController_CreateEquipment_Throughput() {
        Equipment eq = new Equipment("Oscilloscope", "Lab", EquipmentStatus.AVAILABLE);
        
        Equipment response = equipmentController.createEquipment(eq);
        assertNotNull(response);
    }
}