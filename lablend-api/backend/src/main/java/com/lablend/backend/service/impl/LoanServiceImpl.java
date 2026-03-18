package com.lablend.backend.service.impl;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.service.LoanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final EquipmentRepository equipmentRepository;

    @Autowired
    public LoanServiceImpl(LoanRepository loanRepository, EquipmentRepository equipmentRepository) {
        this.loanRepository = loanRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public Loan createLoan(Long equipmentId, Long userId) {
        
        if (equipmentId == null) {
            throw new IllegalArgumentException("Error: Equipment ID cannot be null");
        }

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new RuntimeException("Error: Equipment not found with ID: " + equipmentId));

        if (equipment.getStatus() == null || !equipment.getStatus().equalsIgnoreCase("Available")) {
            throw new IllegalStateException("Cannot create loan. The requested equipment is currently: " + equipment.getStatus());
        }
 
        Loan newLoan = new Loan();
        newLoan.setEquipmentId(equipmentId);
        newLoan.setUserId(userId);
        newLoan.setLoanDate(LocalDate.now());
        newLoan.setStatus("Active");

        return loanRepository.save(newLoan);
    }
}