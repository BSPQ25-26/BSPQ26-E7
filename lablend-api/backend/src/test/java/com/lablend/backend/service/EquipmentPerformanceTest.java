package com.lablend.backend.service;

import com.github.noconnor.junitperf.JUnitPerfTest;
import com.github.noconnor.junitperf.JUnitPerfTestRequirement;
import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.service.impl.EquipmentServiceImpl;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EquipmentPerformanceTest {

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    @Mock
    private EquipmentRepository equipmentRepository;

    // 1. Successful performance test
    // Use 'totalExecutions' instead of 'invocations'
    @JUnitPerfTest(totalExecutions = 100, threads = 10, durationMs = 2000)
    @JUnitPerfTestRequirement(executionsPerSec = 10, allowedErrorPercentage = 0)
    public void testEquipmentCreation_Success() {
        Equipment eq = new Equipment("Oscilloscope", "Lab", EquipmentStatus.AVAILABLE);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(eq);
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }

    // 2. Failed performance test (Expected to fail)
    @JUnitPerfTest(totalExecutions = 20, threads = 2, durationMs = 1000)
    @JUnitPerfTestRequirement(maxLatency = 1) // Forced to fail: 1ms max latency
    public void testEquipmentCreation_Fail() throws InterruptedException {
        Equipment eq = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(eq);
        
        Thread.sleep(5); // Artificial delay to break the 1ms max latency rule
        
        Equipment created = equipmentService.createEquipment(eq);
        assertNotNull(created);
    }
}