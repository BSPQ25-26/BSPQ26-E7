package com.lablend.backend.controller;

import com.lablend.backend.entity.Loan;
import com.lablend.backend.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for {@link Loan} management operations.
 */
@RestController
@RequestMapping("/api/loans")
public class LoanController {

    @Autowired
    private LoanService loanService;

    /**
     * Retrieves all loan records.
     *
     * @return list of all loans
     */
    @GetMapping
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
    public ResponseEntity<?> createLoan(@RequestBody Loan loan) {
        try {
            Loan createdLoan = loanService.createLoan(loan);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdLoan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates an existing loan record.
     *
     * @param id   loan identifier
     * @param loan updated loan payload
     * @return 200 with the updated loan, 404 when not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<Loan> updateLoan(@PathVariable Long id, @RequestBody Loan loan) {
        try {
            Loan updatedLoan = loanService.updateLoan(id, loan);
            return ResponseEntity.ok(updatedLoan);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Deletes a loan record by its identifier.
     *
     * @param id loan identifier
     * @return 204 when deleted, 404 when not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLoan(@PathVariable Long id) {
        try {
            loanService.deleteLoan(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Marks a loan as returned and makes the equipment available again.
     *
     * @param id loan identifier
     * @return 200 with the completed loan
     */
    @PutMapping("/{id}/return")
    public ResponseEntity<?> returnLoan(@PathVariable Long id) {
        try {
            Loan completedLoan = loanService.returnLoan(id);
            return ResponseEntity.ok(completedLoan);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    /**
     * GET endpoint for administrators to list all overdue loans.
     */
    @GetMapping("/overdue")
    public ResponseEntity<java.util.List<com.lablend.backend.dto.OverdueLoanDTO>> getOverdueLoans() {
        return ResponseEntity.ok(loanService.getOverdueLoans());
    }
}