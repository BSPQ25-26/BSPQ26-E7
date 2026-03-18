package com.lablend.backend.repository;

import com.lablend.backend.entity.Equipment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.Rollback;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Rollback(false)
public class EquipmentRepositoryTest {

    @Autowired
    private EquipmentRepository equipmentRepository;

    private Equipment equipment;

    @BeforeEach
    public void setUp() {
        equipment = new Equipment();
        equipment.setName("Microscope");
        equipment.setType("Optical");
        equipment.setStatus("Available");
    }

    @Test
    public void testSaveEquipment() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        assertThat(savedEquipment).isNotNull();
        assertThat(savedEquipment.getId()).isGreaterThan(0);
    }

    @Test
    public void testFindEquipmentById() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        Equipment foundEquipment = equipmentRepository.findById(savedEquipment.getId()).orElse(null);
        assertThat(foundEquipment).isNotNull();
        assertThat(foundEquipment.getName()).isEqualTo(equipment.getName());
    }

    @Test
    public void testUpdateEquipment() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        savedEquipment.setName("Updated Microscope");
        Equipment updatedEquipment = equipmentRepository.save(savedEquipment);
        assertThat(updatedEquipment.getName()).isEqualTo("Updated Microscope");
    }

    @Test
    public void testDeleteEquipment() {
        Equipment savedEquipment = equipmentRepository.save(equipment);
        equipmentRepository.delete(savedEquipment);
        Equipment deletedEquipment = equipmentRepository.findById(savedEquipment.getId()).orElse(null);
        assertThat(deletedEquipment).isNull();
    }
}