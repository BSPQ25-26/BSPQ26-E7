package com.lablend.backend.service;

import com.lablend.backend.entity.Loan;

public interface LoanService {
    Loan createLoan(Long equipmentId, Long userId);
}