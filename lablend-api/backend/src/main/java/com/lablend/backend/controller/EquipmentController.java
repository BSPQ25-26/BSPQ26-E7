package com.lablend.backend.controller;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.service.EquipmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.access.prepost.PreAuthorize;

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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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

    /**
     * Retrieves a paginated list of all equipment records.
     * Accepts optional query parameters to control pagination behavior.
     * If no parameters are provided, defaults to the first page with 10 items per page.
     * @param page The zero-based page number to retrieve (default: 0).
     * @param size The number of items per page (default: 10).
     * @return A Page object containing the equipment entities for the requested page,
     *         along with metadata such as total pages and total elements.
     */
    @GetMapping
    public Page<Equipment> getAll(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        return equipmentService.getAllEquipmentPaged(PageRequest.of(page, size));
    }
}