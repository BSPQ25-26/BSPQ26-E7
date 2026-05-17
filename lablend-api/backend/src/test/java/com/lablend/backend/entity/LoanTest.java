package com.lablend.backend.entity;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LoanTest {

    @Test
    void testLoanDefaultConstructorAndSetters() {
        Loan loan = new Loan();
        LocalDateTime now = LocalDateTime.now();

        loan.setId(1L);
        loan.setUserId(2L);
        loan.setEquipmentId(3L);
        loan.setLoanDate(now);
        loan.setStatus(LoanStatus.COMPLETED);
        loan.setExtended(true);

        assertEquals(1L, loan.getId());
        assertEquals(2L, loan.getUserId());
        assertEquals(3L, loan.getEquipmentId());
        assertEquals(now, loan.getLoanDate());
        assertEquals(LoanStatus.COMPLETED, loan.getStatus());
        assertTrue(loan.isExtended());
    }

    @Test
    void testLoanParameterizedConstructor() {
        LocalDateTime now = LocalDateTime.now();
        Loan loan = new Loan(5L, 10L, now, LoanStatus.ACTIVE);

        assertNull(loan.getId());
        assertEquals(5L, loan.getUserId());
        assertEquals(10L, loan.getEquipmentId());
        assertEquals(now, loan.getLoanDate());
        assertEquals(LoanStatus.ACTIVE, loan.getStatus());
        assertFalse(loan.isExtended());
    }
}
