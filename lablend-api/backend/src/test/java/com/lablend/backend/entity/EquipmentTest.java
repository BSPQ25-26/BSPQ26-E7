package com.lablend.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EquipmentTest {

    private Equipment equipment;

    @BeforeEach
    public void setUp() {
        equipment = new Equipment();
        equipment.setId(1L);
        equipment.setName("Microscope");
        equipment.setType("Optical");
        equipment.setStatus(EquipmentStatus.AVAILABLE);
    }

    @Test
    public void testEquipmentCreation() {
        assertNotNull(equipment);
        assertEquals(1L, equipment.getId());
        assertEquals("Microscope", equipment.getName());
        assertEquals("Optical", equipment.getType());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
    }

    @Test
    public void testReserveTransitionSuccess() {
        equipment.reserve();
        assertEquals(EquipmentStatus.RESERVED, equipment.getStatus());
    }

    @Test
    public void testReserveTransitionFail() {
        equipment.setStatus(EquipmentStatus.UNDER_MAINTENANCE);
        
        assertThrows(IllegalStateException.class, () -> {
            equipment.reserve();
        });
    }

    @Test
    public void testMaintenanceTransitions() {
        equipment.startMaintenance();
        assertEquals(EquipmentStatus.UNDER_MAINTENANCE, equipment.getStatus());

        equipment.finishMaintenance();
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
    }

    @Test
    public void testVersionFieldExists() {
        equipment.setVersion(1L);
        assertEquals(1L, equipment.getVersion());
    }

    @Test
    public void testEquipmentSettersAndGetters() {
        equipment.setName("Centrifuge");
        assertEquals("Centrifuge", equipment.getName());

        equipment.setType("Mechanical");
        assertEquals("Mechanical", equipment.getType());

        equipment.setStatus(EquipmentStatus.RESERVED); 
        assertEquals(EquipmentStatus.RESERVED, equipment.getStatus());
    }
}