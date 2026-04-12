package com.lablend.backend.repository;

import com.lablend.backend.entity.Loan;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import com.lablend.backend.dto.OverdueLoanDTO;

import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     * Executes a native SQL query to find all active loans past their due date.
     * @param now Current timestamp to compare against due date.
     * @return A list of raw objects from the database join.
     */
    @Query(value = "SELECT l.id AS loanId, u.name AS userName, u.email AS userEmail, " +
               "e.name AS equipmentName, l.due_date AS dueDate " +
               "FROM loans l " +
               "JOIN users u ON l.user_id = u.id " +
               "JOIN equipment e ON l.equipment_id = e.id " +
               "WHERE l.due_date < :now AND l.status = 'ACTIVE'", 
       nativeQuery = true)
    List<Object[]> findOverdueLoansRaw(@Param("now") java.time.LocalDateTime now);
    long countByUserIdAndStatus(Long userId, LoanStatus status);
}