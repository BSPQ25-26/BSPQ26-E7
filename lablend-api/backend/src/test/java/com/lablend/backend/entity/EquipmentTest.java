package com.lablend.backend.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
        equipment.setStatus("Available");
    }

    @Test
    public void testEquipmentCreation() {
        assertNotNull(equipment);
        assertEquals(1L, equipment.getId());
        assertEquals("Microscope", equipment.getName());
        assertEquals("Optical", equipment.getType());
        assertEquals("Available", equipment.getStatus());
    }

    @Test
    public void testEquipmentSettersAndGetters() {
        equipment.setName("Centrifuge");
        assertEquals("Centrifuge", equipment.getName());

        equipment.setType("Mechanical");
        assertEquals("Mechanical", equipment.getType());

        equipment.setStatus("Loaned");
        assertEquals("Loaned", equipment.getStatus());
    }
}