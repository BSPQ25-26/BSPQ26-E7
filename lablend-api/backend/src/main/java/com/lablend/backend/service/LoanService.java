package com.lablend.backend.service;

import com.lablend.backend.entity.Loan;
import java.util.List;
import java.util.Optional;

/**
 * Service interface for {@link Loan} business operations.
 */
public interface LoanService {

    /**
     * Returns all loans.
     *
     * @return list of all loans
     */
    List<Loan> getAllLoans();

    /**
     * Finds a loan by its identifier.
     *
     * @param id loan identifier
     * @return an optional containing the loan if found
     */
    Optional<Loan> getLoanById(Long id);

    /**
     * Creates a new loan and marks the equipment as reserved.
     *
     * @param loan the loan to create
     * @return the persisted loan
     */
    Loan createLoan(Loan loan);

    /**
     * Updates an existing loan.
     *
     * @param id   loan identifier
     * @param loan updated loan data
     * @return the updated loan
     */
    Loan updateLoan(Long id, Loan loan);

    /**
     * Deletes a loan by its identifier.
     *
     * @param id loan identifier
     */
    void deleteLoan(Long id);

    /**
     * Executes the return process for a piece of equipment.
     * Sets the loan status to COMPLETED and reverts the equipment status to AVAILABLE.
     * @param id the identifier of the loan to be returned
     * @return the updated loan entity
     * @throws RuntimeException if the loan or associated equipment is not found
     */
    Loan returnLoan(Long id);

    /**
     * Extends the due date of an active loan. Each loan can only be extended once.
     *
     * @param id the loan identifier
     * @return the updated loan with the extended flag set
     * @throws IllegalStateException if the loan has already been extended or is not active
     * @throws RuntimeException if the loan is not found
     */
    Loan extendLoan(Long id);

    java.util.List<com.lablend.backend.dto.OverdueLoanDTO> getOverdueLoans();
}