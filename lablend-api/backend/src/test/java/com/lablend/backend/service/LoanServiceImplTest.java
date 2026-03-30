package com.lablend.backend.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.service.impl.LoanServiceImpl;

class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private LoanServiceImpl loanService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("null")
    @Test
    void createLoan_WhenEquipmentIsAvailable_ShouldCreateLoan() {
        Equipment equipment = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        equipment.setId(1L);
        
        Loan savedLoan = new Loan();
        savedLoan.setId(100L);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        Loan inputLoan = new Loan();
        inputLoan.setEquipmentId(1L);
        inputLoan.setUserId(2L);

        Loan result = loanService.createLoan(inputLoan);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @SuppressWarnings("null")
    @Test
    void createLoan_WhenEquipmentIsNotAvailable_ShouldThrowException() {
        Equipment equipment = new Equipment("Microscope", "Lab", EquipmentStatus.RESERVED);
        equipment.setId(1L);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        Loan inputLoan = new Loan();
        inputLoan.setEquipmentId(1L);
        inputLoan.setUserId(2L);

        verify(loanRepository, never()).save(any(Loan.class));
    }
    @SuppressWarnings("null")
    @Test
    void createLoan_WhenUserHasThreeActiveLoans_ShouldThrowException() {
        Equipment equipment = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        equipment.setId(1L);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(loanRepository.countByUserIdAndStatus(2L, LoanStatus.ACTIVE)).thenReturn(3L);

        Loan inputLoan = new Loan();
        inputLoan.setEquipmentId(1L);
        inputLoan.setUserId(2L);

        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(inputLoan);
        });

        assertTrue(exception.getMessage().contains("User has reached the maximum limit of 3 active loans"));
        verify(loanRepository, never()).save(any(Loan.class));
    }
}