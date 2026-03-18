package com.lablend.backend.service;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.Loan;
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
        // Inicializa los objetos falsos (Mocks) antes de cada prueba
        MockitoAnnotations.openMocks(this);
    }

    @SuppressWarnings("null")
    @Test
    void createLoan_WhenEquipmentIsAvailable_ShouldCreateLoan() {
        // 1. Preparamos un equipo falso en estado "Available"
        Equipment equipment = new Equipment("Microscope", "Lab", "Available");
        equipment.setId(1L);
        
        Loan savedLoan = new Loan();
        savedLoan.setId(100L); // Simulamos que la BD le ha dado el ID 100

        // Le decimos al repositorio falso lo que tiene que devolver
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));
        when(loanRepository.save(any(Loan.class))).thenReturn(savedLoan);

        // 2. Ejecutamos tu método
        Loan result = loanService.createLoan(1L, 2L);

        // 3. Comprobamos que funciona (que el resultado no es nulo y se ha guardado 1 vez)
        assertNotNull(result);
        assertEquals(100L, result.getId());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @SuppressWarnings("null")
    @Test
    void createLoan_WhenEquipmentIsNotAvailable_ShouldThrowException() {
        // 1. Preparamos un equipo falso en estado "Loaned" (Prestado)
        Equipment equipment = new Equipment("Microscope", "Lab", "Loaned");
        equipment.setId(1L);

        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(equipment));

        // 2. Ejecutamos tu método y esperamos que salte una excepción
        Exception exception = assertThrows(IllegalStateException.class, () -> {
            loanService.createLoan(1L, 2L);
        });

        // 3. Comprobamos que el mensaje de error es exactamente el que tú programaste
        assertTrue(exception.getMessage().contains("Cannot create loan. The requested equipment is currently: Loaned"));
        
        // Comprobamos que el repositorio NUNCA guardó el préstamo en la BD
        verify(loanRepository, never()).save(any(Loan.class));
    }
}