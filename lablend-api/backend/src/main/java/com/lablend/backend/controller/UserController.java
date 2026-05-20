package com.lablend.backend.controller;

import com.lablend.backend.entity.User;
import com.lablend.backend.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing {@link User} entities.
 * Exposes CRUD endpoints under /api/users.
 */
@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management", description = "Endpoints for user profile administration, role-based controls, account blocking, unblocking, and dashboard analytical metrics.")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Creates a new user.
     * @param user The user entity to be created.
     * @return The created user with HTTP 201, or HTTP 400 if the request is invalid.
     */
    @PostMapping
    @Operation(summary = "Create a new user account", description = "Registers a brand new system user identity profile. Validates payload fields to avoid duplicates.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User resource successfully generated and persisted"),
        @ApiResponse(responseCode = "400", description = "Bad Request: Target criteria validation constraint breach or syntax errors in metadata payload")
    })
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User createdUser = userService.createUser(user);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
     /**
     * Updates an existing user by ID.
     * @param id The ID of the user to update.
     * @param user The user object containing the new data.
     * @return The updated user with HTTP 200, or HTTP 404 if not found.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing user profile", description = "Replaces metadata records or structural flags on an active user registry indexed by its database ID key reference.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User profile modifications processed successfully"),
        @ApiResponse(responseCode = "404", description = "Target resource ID context reference not matched")
    })
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(id, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Deletes a user by ID.
     * @param id The ID of the user to delete.
     * @return HTTP 204 if deleted successfully, or HTTP 404 if not found.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user account profile by ID", description = "Performs a hard delete operation dropping target identity structures from system context records entirely.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Resource dropped successfully from system indices"),
        @ApiResponse(responseCode = "404", description = "Target context identity reference key not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
    /**
     * Retrieves all blocked users.
     * Requires ADMIN role.
     *
     * @return List of blocked users with HTTP 200.
     */
    @GetMapping("/blocked")
    @Operation(summary = "Get all blocked user listings", description = "Queries database state tables to retrieve users explicitly flagged under a BLOCKED status criteria context. Requires ADMIN privileges.")
    @ApiResponse(responseCode = "200", description = "Successfully pulled blocked accounts list query arrays")
    public ResponseEntity<List<User>> getBlockedUsers() {
        return ResponseEntity.ok(userService.getBlockedUsers());
    }
    /**
     * Retrieves a user by ID.
     * @param id The ID of the user to retrieve.
     * @return The user with HTTP 200, or HTTP 404 if not found.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get user account details by ID", description = "Queries system tables to display full contextual profiles and status states related to a single user resource.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Target account data structures located and mapped successfully"),
        @ApiResponse(responseCode = "404", description = "User item tracking match not found")
    })
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    /**
     * Retrieves all users.
     * @return A list of all users with HTTP 200.
     */
    @GetMapping
    @Operation(summary = "Get all registered users listings", description = "Compiles a master collection sequence listing every identity account populated across the database environment layer.")
    @ApiResponse(responseCode = "200", description = "Successfully pulled comprehensive master list rows from entity context")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }


    /**
     * Blocks a user by ID. Only USER-role accounts can be blocked.
     * Requires ADMIN role.
     *
     * @param id The ID of the user to block.
     * @return HTTP 200 on success, 404 if not found, 409 if target is an admin.
     */
    @PutMapping("/{id}/block")
    @Operation(summary = "Block a target student user account", description = "Applies access restrictions onto an account by shifting its state to BLOCKED. Restricted strictly to accounts bound under the USER role boundary layers. Requires ADMIN privileges.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account status mutated to BLOCKED successfully"),
        @ApiResponse(responseCode = "409", description = "Conflict: Target user account holds administrative privileges (ADMIN role) and cannot be blocked"),
        @ApiResponse(responseCode = "404", description = "User target reference mapping keys not found")
    })
    public ResponseEntity<?> blockUser(@PathVariable Long id) {
        try {
            userService.blockUser(id);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }


    /**
     * Unblocks a previously blocked user.
     * Requires ADMIN role.
     *
     * @param id The ID of the user to unblock.
     * @return HTTP 200 on success, 404 if not found.
     */
   @PutMapping("/{id}/unblock")
   @Operation(summary = "Unblock a restricted student user account", description = "Restores access capabilities to an account by resetting its structural status state fields back to ACTIVE. Will fail with a 403 response if manual override reviews are flagged. Requires ADMIN privileges.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Account privileges reinstated successfully; state shifted back to ACTIVE"),
        @ApiResponse(responseCode = "403", description = "Forbidden: Administrative constraint block. Account has been flagged as requiring a manual administrative override review"),
        @ApiResponse(responseCode = "404", description = "User database structural link match not found")
    })
    public ResponseEntity<?> unblockUser(@PathVariable Long id) {
        try {
            User user = userService.getUserById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            if (user.isRequiresManualReview()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Account requires a manual administrative override review before unblocking.");
            }

            userService.unblockUser(id);
            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * GET endpoint acting as an Administrative Dashboard query.
     * Fetches all students flagged with active penalties or waiting for a manual account review.
     * * @return List of flagged users with HTTP 200 OK.
     */
    @GetMapping("/dashboard/flagged")
    @Operation(summary = "Get flagged students compilation dataset", description = "Runs stream filtration heuristics over user schemas to map and group students displaying current active penalties or waiting for an administrative override review.")
    @ApiResponse(responseCode = "200", description = "Successfully parsed target schemas and generated data tables matching criteria")
    public ResponseEntity<List<User>> getFlaggedStudentsDashboard() {
        List<User> flaggedUsers = userService.getAllUsers().stream()
                .filter(u -> u.getStatus() == com.lablend.backend.entity.UserStatus.BLOCKED 
                          || u.isRequiresManualReview())
                .collect(java.util.stream.Collectors.toList());
                
        return ResponseEntity.ok(flaggedUsers);
    }
   
}