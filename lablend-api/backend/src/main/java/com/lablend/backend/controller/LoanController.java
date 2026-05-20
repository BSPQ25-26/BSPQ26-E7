package com.lablend.backend.controller;

import com.lablend.backend.entity.Loan;
import com.lablend.backend.service.LoanService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for {@link Loan} management operations.
 */
@RestController
@RequestMapping("/api/loans")
@Tag(name = "Loan Management", description = "Endpoints for managing laboratory equipment loans, processing returns, managing extensions, and tracking overdue items.")
public class LoanController {

    @Autowired
    private LoanService loanService;

    /**
     * Retrieves all loan records.
     *
     * @return list of all loans
     */
    @GetMapping
    @Operation(summary = "Get all loan records", description = "Retrieves a comprehensive history and active list of all laboratory equipment loans.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved all loans list")
    public ResponseEntity<java.util.List<Loan>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }

    /**
     * Retrieves a loan by its identifier.
     *
     * @param id loan identifier
     * @return 200 with the loan when found, 404 otherwise
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a loan by ID", description = "Fetches tracking data and timeline metrics of a specific loan entry via its database ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Loan details successfully retrieved"),
        @ApiResponse(responseCode = "404", description = "Loan record not found with the specified ID")
    })
    public ResponseEntity<Loan> getLoanById(@PathVariable Long id) {
        return loanService.getLoanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Creates a new loan. The equipment must be available; its status
     * will be changed to RESERVED upon success.
     *
     * @param loan loan payload (must include userId and equipmentId)
     * @return 201 with the created loan, or 400 if the request is invalid
     */
    @PostMapping
    @Operation(summary = "Create a new equipment loan", description = "Registers an equipment request allocation. The target equipment status must be free; transitions to RESERVED status immediately upon success.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Loan allocation entry successfully initialized"),
        @ApiResponse(responseCode = "400", description = "Bad Request: Supplied request entity syntax missing critical references (userId/equipmentId)"),
        @ApiResponse(responseCode = "409", description = "Conflict: Target equipment item is already reserved or active in another loan session")
    })
    public ResponseEntity<?> createLoan(@RequestBody Loan loan) {
        Loan createdLoan = loanService.createLoan(loan);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);
    }

    /**
     * Updates an existing loan record.
     *
     * @param id   loan identifier
     * @param loan updated loan payload
     * @return 200 with the updated loan, 404 when not found
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update an existing loan tracking schema", description = "Overrides timestamps, system metadata or status data points on an existing active loan sheet.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Loan logs successfully patched"),
        @ApiResponse(responseCode = "404", description = "Target loan document ID match not found")
    })
    public ResponseEntity<Loan> updateLoan(@PathVariable Long id, @RequestBody Loan loan) {
        Loan updatedLoan = loanService.updateLoan(id, loan);
        return ResponseEntity.ok(updatedLoan);
    }

    /**
     * Deletes a loan record by its identifier.
     *
     * @param id loan identifier
     * @return 204 when deleted, 404 when not found
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a loan record", description = "Hard deletes a legacy or mistyped loan application index directly out of backend store.")
    @ApiResponses({
        @ApiResponse(responseCode = "24", description = "Loan instance context discarded successfully"),
        @ApiResponse(responseCode = "404", description = "No tracking context exists for target reference")
    })
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        loanService.deleteLoan(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Marks a loan as returned and makes the equipment available again.
     *
     * @param id loan identifier
     * @return 200 with the completed loan
     */
    @PutMapping("/{id}/return")
    @Operation(summary = "Process equipment return", description = "Concludes an active loan lifecycle. Evaluates tracking deadlines, records resolution metrics, and switches equipment status back to AVAILABLE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Equipment return acknowledged; lifecycle ended successfully"),
        @ApiResponse(responseCode = "404", description = "Loan entity not matched")
    })
    public ResponseEntity<?> returnLoan(@PathVariable Long id) {
        Loan completedLoan = loanService.returnLoan(id);
        return ResponseEntity.ok(completedLoan);
    }

    /**
     * Extends the duration of an active loan. Each loan can only be extended once.
     *
     * @param id loan identifier
     * @return 200 with the extended loan, 409 if already extended or not active, 404 if not found
     */
    @PutMapping("/{id}/extend")
    @Operation(summary = "Extend active loan duration timeline", description = "Grants extra runtime buffer margins over current deadlines. Business logic limitation: Every unique loan instance is eligible to call this feature exactly once.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Loan timeline extended successfully"),
        @ApiResponse(responseCode = "409", description = "Conflict: Request rejected. Timeline has already consumed its single allocation expansion buffer or the current status is inactive"),
        @ApiResponse(responseCode = "404", description = "Loan key references not matched")
    })
    public ResponseEntity<?> extendLoan(@PathVariable Long id) {
        Loan extendedLoan = loanService.extendLoan(id);
        return ResponseEntity.ok(extendedLoan);
    }

    /**
     * GET endpoint for administrators to list all overdue loans.
     */
    @GetMapping("/overdue")
    @Operation(summary = "Get all overdue loans items", description = "Scans active data sets and returns a computed payload mapping tracking elements breaches. Intended for administrative audit interfaces.")
    @ApiResponse(responseCode = "200", description = "Overdue projection data summary compiled and returned successfully")
    public ResponseEntity<java.util.List<com.lablend.backend.dto.OverdueLoanDTO>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.getOverdueLoans());
    }
}
