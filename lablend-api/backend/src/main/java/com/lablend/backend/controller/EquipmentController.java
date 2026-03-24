package com.lablend.backend.controller;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for {@link Equipment} management operations.
 */
@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    @Autowired
    private EquipmentService equipmentService;

    /**
     * Retrieves all equipment records.
     *
     * @return list of all equipment
     */
    @GetMapping
    public List<Equipment> getAllEquipment() {
        return equipmentService.getAllEquipment();
    }

    /**
     * Retrieves one equipment record by identifier.
     *
     * @param id equipment identifier
     * @return 200 with equipment when found, 404 otherwise
     */
    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return equipmentService.getEquipmentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new equipment record.
     *
     * @param equipment equipment payload
     * @return created equipment
     */
    @PostMapping
    public Equipment createEquipment(@RequestBody Equipment equipment) {
        return equipmentService.createEquipment(equipment);
    }

    /**
     * Updates an existing equipment record.
     *
     * @param id equipment identifier
     * @param equipment updated equipment payload
     * @return 200 with updated equipment when found, 404 otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id, @RequestBody Equipment equipment) {
        Equipment updatedEquipment = equipmentService.updateEquipment(id, equipment);
        if (updatedEquipment != null) {
            return ResponseEntity.ok(updatedEquipment);
        }
        return ResponseEntity.notFound().build();
    }

    /**
     * Deletes an equipment record by identifier.
     *
     * @param id equipment identifier
     * @return 204 when deleted, 404 when the equipment does not exist
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        if (equipmentService.getEquipmentById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        equipmentService.deleteEquipment(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Reserves an equipment item if it is currently available.
     *
     * @param id equipment identifier
     * @return 200 with updated equipment when reserved, 400 when transition is invalid,
     *         or 404 when equipment is not found
     */
    @PutMapping("/{id}/reserve")
    public ResponseEntity<?> reserveEquipment(@PathVariable Long id) {
        try {
            Equipment reserved = equipmentService.reserveEquipment(id);
            return ResponseEntity.ok(reserved);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}