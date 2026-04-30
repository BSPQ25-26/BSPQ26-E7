package com.lablend.backend.controller;

import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.service.LoanService;
import com.lablend.backend.dto.OverdueLoanDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.lablend.backend.auth.filter.JwtAuthenticationFilter;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LoanController.class)
@AutoConfigureMockMvc(addFilters = false)
class LoanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void testGetAllLoans() throws Exception {
        Loan loan = new Loan(1L, 2L, LocalDateTime.now(), LoanStatus.ACTIVE);
        loan.setId(10L);

        when(loanService.getAllLoans()).thenReturn(List.of(loan));

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10L))
                .andExpect(jsonPath("$[0].userId").value(1L))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test
    void testGetLoanById() throws Exception {
        Loan loan = new Loan(1L, 2L, LocalDateTime.now(), LoanStatus.ACTIVE);
        loan.setId(10L);

        when(loanService.getLoanById(10L)).thenReturn(Optional.of(loan));

        mockMvc.perform(get("/api/loans/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void testCreateLoan() throws Exception {
        Loan createdLoan = new Loan(1L, 2L, LocalDateTime.now(), LoanStatus.ACTIVE);
        createdLoan.setId(10L);

        when(loanService.createLoan(any(Loan.class))).thenReturn(createdLoan);

        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "userId": 1,
                            "equipmentId": 2,
                            "status": "ACTIVE"
                        }
                        """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10L));
    }

    @Test
    void testReturnLoan() throws Exception {
        Loan returnedLoan = new Loan(1L, 2L, LocalDateTime.now(), LoanStatus.COMPLETED);
        returnedLoan.setId(10L);

        when(loanService.returnLoan(10L)).thenReturn(returnedLoan);

        mockMvc.perform(put("/api/loans/10/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void testDeleteLoan() throws Exception {
        mockMvc.perform(delete("/api/loans/10"))
                .andExpect(status().isNoContent());
    }
}
