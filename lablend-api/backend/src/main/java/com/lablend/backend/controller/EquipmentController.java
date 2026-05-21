package com.lablend.backend.controller;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.service.EquipmentService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * REST controller for {@link Equipment} management operations.
 */
@RestController
@RequestMapping("/api/equipment")
@Tag(name = "Equipment Management", description = "Endpoints for managing laboratory equipment inventory, tracking availability states, and reservations.")
@Validated
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
    @Operation(summary = "Get equipment by ID", description = "Fetches the full details of a specific piece of laboratory equipment using its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Equipment successfully found and returned"),
        @ApiResponse(responseCode = "404", description = "Equipment not found with the specified ID")
    })
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
    @Operation(summary = "Create a new equipment record", description = "Adds a brand new equipment item to the inventory system. Requires ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Equipment successfully registered into inventory"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Access denied. Missing required ADMIN privileges.")
    })
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
    @Operation(summary = "Update an existing equipment item", description = "Modifies data sheets, naming, or attributes of an existing equipment record by ID. Requires ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Equipment successfully updated"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Missing ADMIN role"),
        @ApiResponse(responseCode = "404", description = "Equipment not found with the provided ID")
    })
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
    @Operation(summary = "Delete an equipment item by ID", description = "Permanently drops an equipment record out of the inventory registry database. Requires ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "24", description = "Equipment successfully purged from database"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Missing ADMIN privileges"),
        @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
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
    @Operation(summary = "Reserve an equipment item", description = "Transitions an equipment piece state to reserved if currently available. Requires ADMIN role.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Equipment successfully locked into a reservation state"),
        @ApiResponse(responseCode = "400", description = "Bad Request: Illegal state transition (e.g., equipment is broken or already loaned)"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Missing ADMIN privileges"),
        @ApiResponse(responseCode = "404", description = "Equipment not found")
    })
    public ResponseEntity<?> reserveEquipment(@PathVariable Long id) {
        Equipment reserved = equipmentService.reserveEquipment(id);
        return ResponseEntity.ok(reserved);
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
    @Operation(summary = "Get paginated list of equipment", description = "Fetches a slice of the equipment table. Supports zero-based offset page indexing and custom page sizing elements.")
    @ApiResponse(responseCode = "200", description = "Successfully pulled paginated chunk of items from database context")
    public Page<Equipment> getAll(
        @RequestParam(defaultValue = "0") @Min(value = 0, message = "Page index must be zero or greater") int page,
        @RequestParam(defaultValue = "10") @Min(value = 1, message = "Page size must be at least 1") @Max(value = 100, message = "Page size cannot exceed 100") int size
    ) {
        return equipmentService.getAllEquipmentPaged(PageRequest.of(page, size));
    }
}
