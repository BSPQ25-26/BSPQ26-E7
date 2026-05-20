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
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("perf")
@org.junit.jupiter.api.extension.ExtendWith(JUnitPerfInterceptor.class)
public class EquipmentPerformanceTest {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentPerformanceTest.class);
    private static final AtomicLong SEQUENCE = new AtomicLong();

    @JUnitPerfTestActiveConfig
    public static final JUnitPerfReportingConfig PERF_CONFIG = JUnitPerfReportingConfig.builder()
            .reportGenerator(new HtmlReportGenerator("target/site/perf-reports/performance_report.html"))
            .build();

    @Autowired
    private EquipmentService equipmentService;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Test
    @JUnitPerfTest(threads = 10, durationMs = 2000, maxExecutionsPerSecond = 100)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, executionsPerSec = 10, meanLatency = 100.0f, maxLatency = 500.0f)
    public void testEquipmentCreation_Throughput_Success() {
        logger.debug("Ejecutando prueba de rendimiento: Throughput");
        long suffix = SEQUENCE.incrementAndGet();
        Equipment eq = new Equipment("Oscilloscope-" + suffix, "Lab-" + suffix, EquipmentStatus.AVAILABLE);

        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
        long createdId = created.getId().longValue();
        assertNotNull(equipmentRepository.findById(createdId).orElseThrow());
    }

    @Test
    @JUnitPerfTest(threads = 5, durationMs = 1000)
    @JUnitPerfTestRequirement(allowedErrorPercentage = 0, maxLatency = 200.0f, meanLatency = 100.0f)
    public void testEquipmentCreation_Duration_Success() {
        logger.debug("Ejecutando prueba de rendimiento: Duration");
        long suffix = SEQUENCE.incrementAndGet();
        Equipment eq = new Equipment("Microscope-" + suffix, "Lab-" + suffix, EquipmentStatus.AVAILABLE);

        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
        long createdId = created.getId().longValue();
        assertNotNull(equipmentRepository.findById(createdId).orElseThrow());
    }
}