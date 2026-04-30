package com.lablend.backend.repository;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class EquipmentRepositoryTest {

    @Autowired
    private EquipmentRepository equipmentRepository;

    private Equipment equipment;

    @BeforeEach
    public void setUp() {
        equipment = new Equipment();
        equipment.setName("Microscope");
        equipment.setType("Optical");
        equipment.setStatus(EquipmentStatus.AVAILABLE);
    }

    @Test
    public void testSaveEquipment() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        assertThat(savedEquipment).isNotNull();
        assertThat(savedEquipment.getId()).isGreaterThan(0);
        assertThat(savedEquipment.getStatus()).isEqualTo(EquipmentStatus.AVAILABLE);
        assertThat(savedEquipment.getVersion()).isEqualTo(0L);
    }

    @Test
    public void testFindEquipmentById() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        Equipment foundEquipment = equipmentRepository.findById(savedEquipment.getId()).orElse(null);
        assertThat(foundEquipment).isNotNull();
        assertThat(foundEquipment.getName()).isEqualTo(equipment.getName());
        assertThat(foundEquipment.getStatus()).isEqualTo(EquipmentStatus.AVAILABLE);
    }

    @Test
    public void testUpdateEquipment() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        savedEquipment.setName("Updated Microscope");
        Equipment updatedEquipment = equipmentRepository.save(savedEquipment);
        assertThat(updatedEquipment.getName()).isEqualTo("Updated Microscope");
    }

    @Test
    public void testUpdateEquipmentStatusWithPattern() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        equipmentRepository.flush(); // <--- IMPORTANTE: Fuerza el insert en la DB

        savedEquipment.reserve(); 
    
        Equipment updatedEquipment = equipmentRepository.saveAndFlush(savedEquipment); // <--- Usa saveAndFlush
    
        assertThat(updatedEquipment.getStatus()).isEqualTo(EquipmentStatus.RESERVED);
    
        assertThat(updatedEquipment.getVersion()).isEqualTo(1L);
    }   

    @Test
    public void testDeleteEquipment() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        equipmentRepository.delete(savedEquipment);
        Equipment deletedEquipment = equipmentRepository.findById(savedEquipment.getId()).orElse(null);
        assertThat(deletedEquipment).isNull();
    }
}