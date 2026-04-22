package com.lablend.backend.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.lablend.backend.dto.OverdueLoanDTO;
import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.service.LoanService;

import jakarta.transaction.Transactional;

/**
 * Implementation of {@link LoanService}.
 */
@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final EquipmentRepository equipmentRepository;

    /**
     * Constructs the service with required repositories.
     *
     * @param loanRepository      loan data access
     * @param equipmentRepository equipment data access
     */
    public LoanServiceImpl(LoanRepository loanRepository, EquipmentRepository equipmentRepository) {
        this.loanRepository = loanRepository;
        this.equipmentRepository = equipmentRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Loan createLoan(Loan loan) {
        long activeLoansCount = loanRepository.countByUserIdAndStatus(loan.getUserId(), LoanStatus.ACTIVE);
        
        if (activeLoansCount >= 3) {
            throw new IllegalStateException("User has reached the maximum limit of 3 active loans");
        }

        Equipment equipment = equipmentRepository.findById(loan.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + loan.getEquipmentId()));

        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new RuntimeException("Equipment is not available for loan. Current status: " + equipment.getStatus());
        }

        loan.setLoanDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.ACTIVE);

        equipment.setStatus(EquipmentStatus.RESERVED);
        equipmentRepository.save(equipment);

        return loanRepository.save(loan);
    }

    /** {@inheritDoc} */
    @Override
    public java.util.List<Loan> getAllLoans() {
        return loanRepository.findAll();
    }

    /** {@inheritDoc} */
    @Override
    public java.util.Optional<Loan> getLoanById(Long id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        return loanRepository.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public Loan updateLoan(Long id, Loan loanDetails) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));

        // Release equipment when the loan is completed or cancelled
        if (loan.getStatus() == LoanStatus.ACTIVE
                && (loanDetails.getStatus() == LoanStatus.COMPLETED
                    || loanDetails.getStatus() == LoanStatus.CANCELLED)) {
            Equipment equipment = equipmentRepository.findById(loan.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + loan.getEquipmentId()));
            equipment.setStatus(EquipmentStatus.AVAILABLE);
            equipmentRepository.save(equipment);
        }

        loan.setUserId(loanDetails.getUserId());
        loan.setEquipmentId(loanDetails.getEquipmentId());
        loan.setStatus(loanDetails.getStatus());
        
        return loanRepository.save(loan);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteLoan(Long id) {
        if (id == null) throw new IllegalArgumentException("Id cannot be null");
        
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));

        // Release equipment if the loan being deleted is still active
        if (loan.getStatus() == LoanStatus.ACTIVE) {
            Equipment equipment = equipmentRepository.findById(loan.getEquipmentId())
                    .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + loan.getEquipmentId()));
            equipment.setStatus(EquipmentStatus.AVAILABLE);
            equipmentRepository.save(equipment);
        }

        loanRepository.delete(loan);
    }

    /** {@inheritDoc} */
    @Transactional
    @Override
    public Loan returnLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));

        loan.setStatus(LoanStatus.COMPLETED);

        equipmentRepository.findById(loan.getEquipmentId()).ifPresent(equipment -> {
            equipment.setStatus(EquipmentStatus.AVAILABLE);
            equipmentRepository.save(equipment);
        });

        return loanRepository.save(loan);
    }

    /**
     * Retrieves all active loans that are past their due date.
     * Maps raw database results into OverdueLoanDTO objects.
     * @return List of overdue loan details for admin use.
     */
    @Override
    public List<OverdueLoanDTO> getOverdueLoans() {
        List<Object[]> results = loanRepository.findOverdueLoansRaw(java.time.LocalDateTime.now());
        
        return results.stream().map(result -> new OverdueLoanDTO(
            ((Number) result[0]).longValue(), 
            (String) result[1],               
            (String) result[2],               
            (String) result[3],               
            ((java.sql.Timestamp) result[4]).toLocalDateTime() 
        )).collect(java.util.stream.Collectors.toList());
    }
}
