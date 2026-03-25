package com.lablend.backend.service.impl;

import com.lablend.backend.entity.Equipment;
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
        // Verify the equipment exists and is available
        Equipment equipment = equipmentRepository.findById(loan.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("Equipment not found with id: " + loan.getEquipmentId()));

        if (!"Available".equalsIgnoreCase(equipment.getStatus())) {
            throw new RuntimeException("Equipment is not available for loan. Current status: " + equipment.getStatus());
        }

        // Set loan defaults
        loan.setLoanDate(LocalDateTime.now());
        loan.setStatus(LoanStatus.ACTIVE);

        // Mark equipment as loaned
        equipment.setStatus("Loaned");
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
        return loanRepository.findById(id);
    }

    /** {@inheritDoc} */
    @Override
    public Loan updateLoan(Long id, Loan loanDetails) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));

        loan.setUserId(loanDetails.getUserId());
        loan.setEquipmentId(loanDetails.getEquipmentId());
        loan.setStatus(loanDetails.getStatus());
        
        return loanRepository.save(loan);
    }

    /** {@inheritDoc} */
    @Override
    public void deleteLoan(Long id) {
        Loan loan = loanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Loan not found with id: " + id));
        loanRepository.delete(loan);
    }
}
