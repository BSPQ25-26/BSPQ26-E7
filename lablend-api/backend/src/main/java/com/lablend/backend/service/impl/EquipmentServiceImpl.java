package com.lablend.backend.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.service.EquipmentService;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


/**
 * Implementation of the EquipmentService interface.
 * handles the business logic for laboratory equipment, ensuring data consistency 
 * through transactional operations and state management.
 */
@Service
public class EquipmentServiceImpl implements EquipmentService {

    private static final Logger logger = LoggerFactory.getLogger(EquipmentServiceImpl.class);

    private final EquipmentRepository equipmentRepository;

    /**
     * Constructor-based dependency injection for the equipment repository.
     * @param equipmentRepository The persistence repository for equipment data.
     */
    @Autowired
    public EquipmentServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    /**
     * Persists a new equipment record into the database.
     * @param equipment The equipment entity to be created.
     * @return The saved equipment instance with its generated ID.
     */
    @Override
    public Equipment createEquipment(Equipment equipment) {
        logger.info("Creating new equipment: {}", equipment.getName());
        return equipmentRepository.save(equipment);
    }

    /**
     * Retrieves an equipment record by its unique identifier.
     * @param id The ID of the equipment to find.
     * @return An Optional containing the equipment if found, or empty if not.
     */
    @Override
    public Optional<Equipment> getEquipmentById(Long id) {
        logger.debug("Fetching equipment with ID: {}", id);
        return equipmentRepository.findById(id);
    }

    /**
     * Retrieves all equipment records currently stored in the system.
     * @return A list of all equipment entities.
     */
    @Override
    public List<Equipment> getAllEquipment() {
        logger.debug("Fetching all equipment");
        return equipmentRepository.findAll();
    }

    /**
     * Updates an existing equipment record.
     * This method is transactional to ensure that name, type, and state 
     * transitions are updated atomically.
     * @param id The ID of the equipment to update.
     * @param equipment The equipment object containing the new data.
     * @return The updated and persisted equipment entity, or null if it does not exist.
     */
    @Override
    @Transactional
    public Equipment updateEquipment(Long id, Equipment equipment) {
        logger.info("Attempting to update equipment with ID: {}", id);
        return equipmentRepository.findById(id)
                .map(existingEquipment -> {
                    existingEquipment.setName(equipment.getName());
                    existingEquipment.setType(equipment.getType());

                    if (!existingEquipment.getStatus().equals(equipment.getStatus())) {
                    applyStateTransition(existingEquipment, equipment.getStatus());
                }
                
                logger.info("Successfully updated equipment with ID: {}", id);
                return equipmentRepository.save(existingEquipment);
            })
            .orElseGet(() -> {
                logger.warn("Equipment with ID: {} not found for update", id);
                return null;
            });
    }

    /**
     * Internal helper method to route state changes through the entity's 
     * business logic methods. This ensures that state-specific rules 
     * (like maintenance or reservations) are respected.
     * @param equipment The equipment entity to modify.
     * @param newStatus The target status to transition to.
     */
    private void applyStateTransition(Equipment equipment, EquipmentStatus newStatus) {
        logger.debug("Applying state transition to {} for equipment ID: {}", newStatus, equipment.getId());
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

    /**
     * Removes an equipment record from the system.
     * @param id The ID of the equipment to delete.
     */
    @Override
    public void deleteEquipment(Long id) {
        logger.info("Deleting equipment with ID: {}", id);
        equipmentRepository.deleteById(id);
    }

    /**
     * Specifically handles the reservation logic for a piece of equipment.
     * Marks the operation as transactional to prevent data inconsistency and 
     * invokes the internal state transition logic within the entity.
     * @param id The ID of the equipment to reserve.
     * @return The updated equipment with RESERVED status.
     * @throws RuntimeException if the equipment is not found.
     * @throws IllegalStateException if the reservation transition is invalid.
     */
    @Transactional
    @Override
    public Equipment reserveEquipment(Long id) {
        logger.info("Attempting to reserve equipment with ID: {}", id);
        Equipment equipment = equipmentRepository.findById(id)
        .orElseThrow(() -> {
            logger.error("Failed to reserve. Equipment not found with ID: {}", id);
            return new RuntimeException("Equipment not found");
        });
    
        equipment.reserve(); 
        logger.info("Successfully reserved equipment with ID: {}", id);
    
        return equipmentRepository.save(equipment);
    }
}