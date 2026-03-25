package com.lablend.backend.service.impl;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

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
    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository, EquipmentRepository equipmentRepository) {
        this.loanRepository = loanRepository;
        this.equipmentRepository = equipmentRepository;
    }

    /** {@inheritDoc} */
    @Override
    public Loan createLoan(Loan loan) {
        Equipment equipment = equipmentRepository.findById(loan.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + loan.getEquipmentId()));

        if (equipment.getStatus() != EquipmentStatus.AVAILABLE) {
            throw new RuntimeException("Equipment is not available for loan. Current status: " + equipment.getStatus());
        }

        loan.setLoanDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.ACTIVE);

        equipment.reserve();
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
}