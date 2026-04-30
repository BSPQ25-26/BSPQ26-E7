package com.lablend.backend.repository;

import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {

    /**
     
Executes a native SQL query to find all active loans past their due date (e.g. 14 days old).
@param cutoffDate Timestamp to compare against loan_date.
@return A list of raw objects from the database join.*/
@Query(value = "SELECT l.id AS loanId, u.name AS userName, u.email AS userEmail, " +"e.name AS equipmentName, l.loan_date AS loanDate " +"FROM loans l " +"JOIN users u ON l.user_id = u.id " +"JOIN equipment e ON l.equipment_id = e.id " +"WHERE l.loan_date < :cutoffDate AND l.status = 'ACTIVE'", 
   nativeQuery = true)
List<Object[]> findOverdueLoansRaw(@Param("cutoffDate") LocalDateTime cutoffDate);

    long countByUserIdAndStatus(Long userId, LoanStatus status);

    // Consulta JPQL correcta usando un parámetro para la fecha y para el enum
    @Query("SELECT l FROM Loan l WHERE l.loanDate < :cutoffDate AND l.status = :status")
    List<Loan> findOverdueLoans(@Param("cutoffDate") LocalDateTime cutoffDate, @Param("status") LoanStatus status);

}