package com.lablend.backend.repository;

import com.lablend.backend.entity.Loan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for {@link Loan} entities.
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
}
