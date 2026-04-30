package com.lablend.backend.repository;

import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class LoanRepositoryTest {

    @Autowired
    private LoanRepository loanRepository;

    private Loan loan;

    @BeforeEach
    public void setUp() {
        loan = new Loan(1L, 1L, LocalDateTime.now(), LoanStatus.ACTIVE);
    }

    @Test
    public void testSaveLoan() {
        Loan savedLoan = loanRepository.save(loan);
        
        assertThat(savedLoan).isNotNull();
        assertThat(savedLoan.getId()).isGreaterThan(0);
        assertThat(savedLoan.getUserId()).isEqualTo(1L);
        assertThat(savedLoan.getStatus()).isEqualTo(LoanStatus.ACTIVE);
    }

    @Test
    public void testFindLoanById() {
        Loan savedLoan = loanRepository.save(loan);
        
        Loan foundLoan = loanRepository.findById(savedLoan.getId()).orElse(null);
        
        assertThat(foundLoan).isNotNull();
        assertThat(foundLoan.getEquipmentId()).isEqualTo(1L);
    }

    @Test
    public void testCountByUserIdAndStatus() {
        loanRepository.save(loan);
        
        /
        long activeLoans = loanRepository.countByUserIdAndStatus(1L, LoanStatus.ACTIVE);
        assertThat(activeLoans).isEqualTo(1L);

        
        long completedLoans = loanRepository.countByUserIdAndStatus(1L, LoanStatus.COMPLETED);
        assertThat(completedLoans).isEqualTo(0L);
    }

    @Test
    public void testUpdateLoan() {
        Loan savedLoan = loanRepository.save(loan);
        
        savedLoan.setStatus(LoanStatus.COMPLETED);
        savedLoan.setExtended(true);
        Loan updatedLoan = loanRepository.save(savedLoan);
        
        assertThat(updatedLoan.getStatus()).isEqualTo(LoanStatus.COMPLETED);
        assertThat(updatedLoan.isExtended()).isTrue();
    }

    @Test
    public void testDeleteLoan() {
        Loan savedLoan = loanRepository.save(loan);
        
        loanRepository.delete(savedLoan);
        
        Loan deletedLoan = loanRepository.findById(savedLoan.getId()).orElse(null);
        assertThat(deletedLoan).isNull();
    }
}
