package com.lablend.backend.service;

import com.lablend.backend.entity.Loan;
import java.util.List;
import java.util.Optional;

public interface LoanService {
    List<Loan> getAllLoans();
    Optional<Loan> getLoanById(Long id);
    Loan createLoan(Loan loan);
    Loan updateLoan(Long id, Loan loan);
    void deleteLoan(Long id);
}