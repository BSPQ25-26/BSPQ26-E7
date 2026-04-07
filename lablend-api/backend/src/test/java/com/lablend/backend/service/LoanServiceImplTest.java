package com.lablend.backend.service;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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

  @Test
    void returnLoan_Success() {
        // GIVEN
        Long loanId = 1L;
        Long equipmentId = 10L;

        Loan loan = new Loan();
        loan.setId(loanId);
        loan.setEquipmentId(equipmentId);
        loan.setStatus(LoanStatus.ACTIVE);

        Equipment equipment = new Equipment();
        equipment.setId(equipmentId);
        equipment.setStatus(EquipmentStatus.RESERVED);

        when(loanRepository.findById(loanId)).thenReturn(Optional.of(loan));
        when(equipmentRepository.findById(equipmentId)).thenReturn(Optional.of(equipment));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.returnLoan(loanId);

        assertEquals(LoanStatus.COMPLETED, result.getStatus());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
        verify(loanRepository).save(loan);
        verify(equipmentRepository).save(equipment);
    }

    @Test
    void returnLoan_NotFound() {
        when(loanRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> loanService.returnLoan(99L));
    }

    @Test
    void getOverdueLoans_Success() {
        Object[] row = new Object[] {
            1L, 
            "Test User", 
            "test@mail.com", 
            "Microscope", 
            java.sql.Timestamp.valueOf(java.time.LocalDateTime.now().minusDays(1))
        };
        
        java.util.List<Object[]> mockResponse = new java.util.ArrayList<>();
        mockResponse.add(row);
        
        when(loanRepository.findOverdueLoansRaw(any())).thenReturn(mockResponse);

        java.util.List<com.lablend.backend.dto.OverdueLoanDTO> result = loanService.getOverdueLoans();

        assertFalse(result.isEmpty());
        assertEquals("Test User", result.get(0).getUserName());
        assertEquals("Microscope", result.get(0).getEquipmentName());
        
        verify(loanRepository).findOverdueLoansRaw(any());
    }
}