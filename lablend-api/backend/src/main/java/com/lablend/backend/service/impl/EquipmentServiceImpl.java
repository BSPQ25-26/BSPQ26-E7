package com.lablend.backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.service.EquipmentService;

import jakarta.transaction.Transactional;

@Service
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public Equipment createEquipment(Equipment equipment) {
        return equipmentRepository.save(equipment);
    }

    @Override
    public Optional<Equipment> getEquipmentById(Long id) {
        return equipmentRepository.findById(id);
    }

    @Override
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    @Transactional
    public Equipment updateEquipment(Long id, Equipment equipment) {
        return equipmentRepository.findById(id)
                .map(existingEquipment -> {
                    existingEquipment.setName(equipment.getName());
                    existingEquipment.setType(equipment.getType());

                    if (!existingEquipment.getStatus().equals(equipment.getStatus())) {
                    applyStateTransition(existingEquipment, equipment.getStatus());
                }
                
                return equipmentRepository.save(existingEquipment);
            })
            .orElse(null);
    }


    private void applyStateTransition(Equipment equipment, EquipmentStatus newStatus) {
        switch (newStatus) {
            case RESERVED -> equipment.reserve();
            case UNDER_MAINTENANCE -> equipment.startMaintenance();
            case AVAILABLE -> {
                if (equipment.getStatus() == EquipmentStatus.UNDER_MAINTENANCE) {
                equipment.finishMaintenance();
                } 
                else {
                equipment.setStatus(EquipmentStatus.AVAILABLE);
                }
            }
        }
    }


    @Override
    public void deleteEquipment(Long id) {
        equipmentRepository.deleteById(id);
    }

    @Transactional
    @Override
    public Equipment reserveEquipment(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("Equipment not found"));
    
        equipment.reserve(); 
    
        return equipmentRepository.save(equipment);
    }
}