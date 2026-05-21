package com.lablend.backend.service;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserStatus;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.service.impl.LoanServiceImpl;
import com.lablend.backend.dto.OverdueLoanDTO;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
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

class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private EquipmentRepository equipmentRepository;

    @Mock
    private UserRepository userRepository;


    @InjectMocks
    private LoanServiceImpl loanService;


    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("null")
    @Test
    void createLoan_WhenEquipmentIsAvailable_ShouldCreateLoan() {
        User user = new User();
        user.setId(2L);
        user.setStatus(UserStatus.ACTIVE);

        Equipment equipment = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        equipment.setId(1L);

        Loan savedLoan = new Loan();
        savedLoan.setId(100L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(loanRepository.countByUserIdAndStatus(2L, LoanStatus.ACTIVE)).thenReturn(0L);
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
        User user = new User();
        user.setId(2L);
        user.setStatus(UserStatus.ACTIVE);

        Equipment equipment = new Equipment("Microscope", "Lab", EquipmentStatus.RESERVED);
        equipment.setId(1L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(loanRepository.countByUserIdAndStatus(2L, LoanStatus.ACTIVE)).thenReturn(0L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        Loan inputLoan = new Loan();
        inputLoan.setEquipmentId(1L);
        inputLoan.setUserId(2L);

        assertThrows(RuntimeException.class, () -> loanService.createLoan(inputLoan));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnLoan_Success() {
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
        
        List<Object[]> mockResponse = new ArrayList<>();
        mockResponse.add(row);
        
        when(loanRepository.findOverdueLoansRaw(any())).thenReturn(mockResponse);

        List<OverdueLoanDTO> result = loanService.getOverdueLoans();

        assertFalse(result.isEmpty());
        assertEquals("Test User", result.get(0).getUserName());
        assertEquals("Microscope", result.get(0).getEquipmentName());
        
        verify(loanRepository).findOverdueLoansRaw(any());
    }

    @SuppressWarnings("null")
    @Test
    void createLoan_WhenUserHasThreeActiveLoans_ShouldThrowException() {
        User user = new User();
        user.setId(2L);
        user.setStatus(UserStatus.ACTIVE);

        Equipment equipment = new Equipment("Microscope", "Lab", EquipmentStatus.AVAILABLE);
        equipment.setId(1L);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(loanRepository.countByUserIdAndStatus(2L, LoanStatus.ACTIVE)).thenReturn(3L);

        Loan inputLoan = new Loan();
        inputLoan.setEquipmentId(1L);
        inputLoan.setUserId(2L);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            loanService.createLoan(inputLoan);
        });

        assertTrue(exception.getMessage().contains("User has reached the maximum limit of 3 active loans"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void extendLoan_Success() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setExtended(false);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);

        Loan result = loanService.extendLoan(1L);

        assertTrue(result.isExtended());
        verify(loanRepository).save(loan);
    }

    @Test
    void extendLoan_AlreadyExtended_ShouldThrowException() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setExtended(true);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            loanService.extendLoan(1L);
        });

        assertTrue(exception.getMessage().contains("already been extended"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void extendLoan_NotActive_ShouldThrowException() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setStatus(LoanStatus.COMPLETED);
        loan.setExtended(false);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            loanService.extendLoan(1L);
        });

        assertTrue(exception.getMessage().contains("Only active loans can be extended"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void updateLoan_Success() {
        Loan existingLoan = new Loan();
        existingLoan.setId(1L);
        existingLoan.setUserId(2L);
        existingLoan.setEquipmentId(3L);
        existingLoan.setStatus(LoanStatus.ACTIVE);

        Loan updatedDetails = new Loan();
        updatedDetails.setUserId(2L);
        updatedDetails.setEquipmentId(3L);
        updatedDetails.setStatus(LoanStatus.ACTIVE);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(existingLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(existingLoan);

        Loan result = loanService.updateLoan(1L, updatedDetails);

        assertNotNull(result);
        assertEquals(LoanStatus.ACTIVE, result.getStatus());
        verify(equipmentRepository, never()).save(any(Equipment.class));
        verify(loanRepository).save(existingLoan);
    }

    @Test
    void updateLoan_ReleaseEquipment_WhenCompleted() {
        Loan existingLoan = new Loan();
        existingLoan.setId(1L);
        existingLoan.setEquipmentId(3L);
        existingLoan.setStatus(LoanStatus.ACTIVE);

        Loan updatedDetails = new Loan();
        updatedDetails.setStatus(LoanStatus.COMPLETED);

        Equipment equipment = new Equipment();
        equipment.setId(3L);
        equipment.setStatus(EquipmentStatus.RESERVED);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(existingLoan));
        when(equipmentRepository.findById(3L)).thenReturn(Optional.of(equipment));
        when(loanRepository.save(any(Loan.class))).thenReturn(existingLoan);

        Loan result = loanService.updateLoan(1L, updatedDetails);

        assertEquals(LoanStatus.COMPLETED, result.getStatus());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
        verify(equipmentRepository).save(equipment);
        verify(loanRepository).save(existingLoan);
    }

    @Test
    void updateLoan_NotFound_ShouldThrowException() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.updateLoan(1L, new Loan());
        });

        assertTrue(exception.getMessage().contains("Loan not found with id: 1"));
    }

    @Test
    void deleteLoan_Active_ReleasesEquipment() {
        Loan existingLoan = new Loan();
        existingLoan.setId(1L);
        existingLoan.setEquipmentId(3L);
        existingLoan.setStatus(LoanStatus.ACTIVE);

        Equipment equipment = new Equipment();
        equipment.setId(3L);
        equipment.setStatus(EquipmentStatus.RESERVED);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(existingLoan));
        when(equipmentRepository.findById(3L)).thenReturn(Optional.of(equipment));

        loanService.deleteLoan(1L);

        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
        verify(equipmentRepository).save(equipment);
        verify(loanRepository).delete(existingLoan);
    }

    @Test
    void deleteLoan_NotActive_DoesNotReleaseEquipment() {
        Loan existingLoan = new Loan();
        existingLoan.setId(1L);
        existingLoan.setEquipmentId(3L);
        existingLoan.setStatus(LoanStatus.COMPLETED);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(existingLoan));

        loanService.deleteLoan(1L);

        verify(equipmentRepository, never()).findById(any());
        verify(equipmentRepository, never()).save(any(Equipment.class));
        verify(loanRepository).delete(existingLoan);
    }

    @Test
    void deleteLoan_NotFound_ShouldThrowException() {
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            loanService.deleteLoan(1L);
        });

        assertTrue(exception.getMessage().contains("Loan not found with id: 1"));
    }

    @Test
    void createLoan_WhenUserIsBlocked_ShouldThrowException() {
        User user = new User();
        user.setId(2L);
        user.setStatus(UserStatus.BLOCKED);

        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        Loan inputLoan = new Loan();
        inputLoan.setEquipmentId(1L);
        inputLoan.setUserId(2L);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            loanService.createLoan(inputLoan);
        });

        assertTrue(exception.getMessage().contains("User is blocked and cannot borrow equipment"));
        verify(loanRepository, never()).save(any(Loan.class));
        verify(equipmentRepository, never()).findById(any());
    }

@Test
    void returnLoan_WhenAlreadyCompleted_ShouldThrowException() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setEquipmentId(10L);
        loan.setStatus(LoanStatus.COMPLETED);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            loanService.returnLoan(1L);
        });

        assertTrue(exception.getMessage().contains("already completed"));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void returnLoan_WhenCancelled_ShouldThrowException() {
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setEquipmentId(10L);
        loan.setStatus(LoanStatus.CANCELLED);

        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            loanService.returnLoan(1L);
        });

        assertTrue(exception.getMessage().contains("already completed"));
        verify(loanRepository, never()).save(any(Loan.class));
    }
}
