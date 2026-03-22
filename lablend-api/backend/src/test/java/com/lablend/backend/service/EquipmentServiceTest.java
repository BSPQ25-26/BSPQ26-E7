package com.lablend.backend.service;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.service.impl.EquipmentServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class EquipmentServiceTest {

    @InjectMocks
    private EquipmentServiceImpl equipmentService;

    @Mock
    private EquipmentRepository equipmentRepository;

    private Equipment equipment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Microscope");
        equipment.setType("Optical");
        equipment.setStatus(EquipmentStatus.AVAILABLE);
        equipment.setVersion(0L);
    }

    @Test
    public void testCreateEquipment() {
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        Equipment createdEquipment = equipmentService.createEquipment(equipment);
        assertNotNull(createdEquipment);
        assertEquals("Microscope", createdEquipment.getName());
        verify(equipmentRepository, times(1)).save(equipment);
    }

    @Test
    public void testGetAllEquipment() {
        when(equipmentRepository.findAll()).thenReturn(Arrays.asList(equipment));
        List<Equipment> equipmentList = equipmentService.getAllEquipment();
        assertEquals(1, equipmentList.size());
        assertEquals("Microscope", equipmentList.get(0).getName());
        verify(equipmentRepository, times(1)).findAll();
    }

    @Test
    public void testReserveEquipmentSuccess() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(i -> i.getArguments()[0]);

        Equipment result = equipmentService.reserveEquipment(1L);

        assertNotNull(result);
        assertEquals(EquipmentStatus.RESERVED, result.getStatus());
        verify(equipmentRepository).save(any(Equipment.class));
    }

    @Test
    public void testReserveEquipmentFail_AlreadyReserved() {
        equipment.setStatus(EquipmentStatus.RESERVED);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        assertThrows(IllegalStateException.class, () -> {
            equipmentService.reserveEquipment(1L);
        });
    }

    @Test
    public void testUpdateEquipmentWithStateTransition() {
        Equipment updatedInfo = new Equipment("Microscope V2", "Optical", EquipmentStatus.RESERVED);
        
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(updatedInfo);

        Equipment result = equipmentService.updateEquipment(1L, updatedInfo);

        assertNotNull(result);
        assertEquals(EquipmentStatus.RESERVED, result.getStatus());
    }

    @Test
    public void testGetEquipmentById() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        Optional<Equipment> foundEquipment = equipmentService.getEquipmentById(1L);
        assertTrue(foundEquipment.isPresent());
        assertEquals("Microscope", foundEquipment.get().getName());
        verify(equipmentRepository, times(1)).findById(1L);
    }

    @Test
    public void testUpdateEquipment() {
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.save(any(Equipment.class))).thenReturn(equipment);
        Equipment updatedEquipment = equipmentService.updateEquipment(1L, equipment);
        assertNotNull(updatedEquipment);
        assertEquals("Microscope", updatedEquipment.getName());
        verify(equipmentRepository, times(1)).save(equipment);
    }

    @Test
    public void testDeleteEquipment() {
        doNothing().when(equipmentRepository).deleteById(1L);
        equipmentService.deleteEquipment(1L);
        verify(equipmentRepository, times(1)).deleteById(1L);
    }
}