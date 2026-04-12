package com.lablend.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    long countByUserIdAndStatus(Long userId, LoanStatus status);
}