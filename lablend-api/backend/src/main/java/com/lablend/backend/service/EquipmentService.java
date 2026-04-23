package com.lablend.backend.service;

import com.lablend.backend.entity.Equipment;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EquipmentService {
    Equipment createEquipment(Equipment equipment);
    Optional<Equipment> getEquipmentById(Long id);
    List<Equipment> getAllEquipment();
    Equipment updateEquipment(Long id, Equipment equipment);
    void deleteEquipment(Long id);
    Equipment reserveEquipment(Long id);
    Page<Equipment> getAllEquipmentPaged(Pageable pageable);
}