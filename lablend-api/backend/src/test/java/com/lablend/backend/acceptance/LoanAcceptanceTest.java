package com.lablend.backend.acceptance;

import com.lablend.backend.entity.Equipment;
import com.lablend.backend.entity.EquipmentStatus;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.User;
import com.lablend.backend.entity.UserRole;
import com.lablend.backend.repository.EquipmentRepository;
import com.lablend.backend.repository.LoanRepository;
import com.lablend.backend.repository.UserRepository;
import com.lablend.backend.service.LoanService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.lablend.backend.entity.UserStatus;
import static org.junit.jupiter.api.Assertions.assertThrows;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=create-drop"
    }
)
public class LoanAcceptanceTest {

    private static final Logger logger = LoggerFactory.getLogger(LoanAcceptanceTest.class);

    @Autowired
    private LoanService loanService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private LoanRepository loanRepository;

    @AfterEach
    void tearDown() {
        loanRepository.deleteAll();
        equipmentRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    public void userCanSuccessfullyBorrowAvailableEquipment() {
        logger.info("Iniciando Test de Aceptacion: Usuario solicita prestamo de equipo");

        User user = new User();
        user.setName("ander_student");
        user.setEmail("ander@deusto.es");
        user.setPassword("12345");
        user.setRole(UserRole.USER);
        user = userRepository.save(user);

        Equipment eq = new Equipment("Osciloscopio Digital", "Electronica", EquipmentStatus.AVAILABLE);
        eq = equipmentRepository.save(eq);

        logger.info("Condiciones iniciales preparadas. Usuario ID: {}, Equipo ID: {}", user.getId(), eq.getId());

        Loan newLoan = new Loan();
        newLoan.setUserId(user.getId());
        newLoan.setEquipmentId(eq.getId());
        
        Loan createdLoan = loanService.createLoan(newLoan);

        assertNotNull(createdLoan.getId());
        assertEquals(user.getId(), createdLoan.getUserId());
        
        Equipment updatedEq = equipmentRepository.findById(eq.getId()).orElseThrow();
        assertEquals(EquipmentStatus.RESERVED, updatedEq.getStatus());

        logger.info("Test de Aceptacion superado: El ciclo completo de prestamo funciono como se esperaba.");
    }

    /**Blocked user test */
    @Test
    public void blockedUserCannotBorrowEquipment() {
        logger.info("Iniciando Test de Aceptacion: Usuario bloqueado no puede solicitar prestamo");

        User blocked = new User();
        blocked.setName("blocked_student");
        blocked.setEmail("blocked@deusto.es");
        blocked.setPassword("12345");
        blocked.setRole(UserRole.USER);
        blocked.setStatus(UserStatus.BLOCKED);
        blocked = userRepository.save(blocked);

        Equipment eq = new Equipment("Microscopio", "Optica", EquipmentStatus.AVAILABLE);
        eq = equipmentRepository.save(eq);

        final Long userId = blocked.getId();
        final Long equipmentId = eq.getId();

        Loan loan = new Loan();
        loan.setUserId(userId);
        loan.setEquipmentId(equipmentId);

        IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> loanService.createLoan(loan)
        );
        assertEquals("User is blocked and cannot borrow equipment", ex.getMessage());

        logger.info("Test de Aceptacion superado: Usuario bloqueado fue rechazado correctamente.");
    }
}