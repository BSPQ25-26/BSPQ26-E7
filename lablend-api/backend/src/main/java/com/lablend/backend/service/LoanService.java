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
}
