package com.lablend.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lablend.backend.entity.Loan;
import com.lablend.backend.entity.LoanStatus;
import com.lablend.backend.service.LoanService;
import com.lablend.backend.dto.OverdueLoanDTO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
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
    private com.lablend.backend.auth.filter.JwtAuthenticationFilter jwtAuthenticationFilter;

    @Autowired
    private ObjectMapper objectMapper;

    private Loan testLoan;

    @BeforeEach
    void setUp() {
        testLoan = new Loan();
        testLoan.setId(1L);
        testLoan.setUserId(2L);
        testLoan.setEquipmentId(3L);
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setExtended(false);
    }

    @Test
    void getAllLoans_ShouldReturnListOfLoans() throws Exception {
        when(loanService.getAllLoans()).thenReturn(Arrays.asList(testLoan));

        mockMvc.perform(get("/api/loans"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].userId").value(2))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andDo(print());
    }

    @Test
    void getLoanById_WhenFound_ShouldReturnLoan() throws Exception {
        when(loanService.getLoanById(1L)).thenReturn(Optional.of(testLoan));

        mockMvc.perform(get("/api/loans/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.userId").value(2));
    }

    @Test
    void getLoanById_WhenNotFound_ShouldReturn404() throws Exception {
        when(loanService.getLoanById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/loans/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createLoan_Success_ShouldReturnCreatedLoan() throws Exception {
        when(loanService.createLoan(any(Loan.class))).thenReturn(testLoan);

        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoan)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createLoan_Conflict_ShouldReturn409() throws Exception {
        when(loanService.createLoan(any(Loan.class))).thenThrow(new IllegalStateException("User limit reached"));

        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoan)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User limit reached"));
    }

    @Test
    void createLoan_BadRequest_ShouldReturn400() throws Exception {
        when(loanService.createLoan(any(Loan.class))).thenThrow(new RuntimeException("Equipment unavailable"));

        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoan)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Equipment unavailable"));
    }

    @Test
    void updateLoan_Success_ShouldReturnUpdatedLoan() throws Exception {
        when(loanService.updateLoan(eq(1L), any(Loan.class))).thenReturn(testLoan);

        mockMvc.perform(put("/api/loans/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoan)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateLoan_NotFound_ShouldReturn404() throws Exception {
        when(loanService.updateLoan(eq(99L), any(Loan.class))).thenThrow(new RuntimeException("Loan not found"));

        mockMvc.perform(put("/api/loans/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoan)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteLoan_Success_ShouldReturn204() throws Exception {
        mockMvc.perform(delete("/api/loans/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteLoan_NotFound_ShouldReturn404() throws Exception {
        doThrow(new RuntimeException("Loan not found")).when(loanService).deleteLoan(99L);

        mockMvc.perform(delete("/api/loans/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnLoan_Success_ShouldReturnReturnedLoan() throws Exception {
        Loan returnedLoan = new Loan();
        returnedLoan.setId(1L);
        returnedLoan.setStatus(LoanStatus.COMPLETED);

        when(loanService.returnLoan(1L)).thenReturn(returnedLoan);

        mockMvc.perform(put("/api/loans/1/return"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("COMPLETED"));
    }

    @Test
    void returnLoan_NotFound_ShouldReturn404() throws Exception {
        when(loanService.returnLoan(99L)).thenThrow(new RuntimeException("Loan not found"));

        mockMvc.perform(put("/api/loans/99/return"))
                .andExpect(status().isNotFound());
    }

    @Test
    void extendLoan_Success_ShouldReturnExtendedLoan() throws Exception {
        Loan extendedLoan = new Loan();
        extendedLoan.setId(1L);
        extendedLoan.setExtended(true);

        when(loanService.extendLoan(1L)).thenReturn(extendedLoan);

        mockMvc.perform(put("/api/loans/1/extend"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.extended").value(true));
    }

    @Test
    void extendLoan_Conflict_ShouldReturn409() throws Exception {
        when(loanService.extendLoan(1L)).thenThrow(new IllegalStateException("Already extended"));

        mockMvc.perform(put("/api/loans/1/extend"))
                .andExpect(status().isConflict());
    }

    @Test
    void extendLoan_NotFound_ShouldReturn404() throws Exception {
        when(loanService.extendLoan(99L)).thenThrow(new RuntimeException("Loan not found"));

        mockMvc.perform(put("/api/loans/99/extend"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOverdueLoans_ShouldReturnList() throws Exception {
        OverdueLoanDTO dto = new OverdueLoanDTO(1L, "Test User", "test@example.com", "Microscope", java.time.LocalDateTime.now());

        when(loanService.getOverdueLoans()).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/loans/overdue"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$[0].loanId").value(1))
                .andExpect(jsonPath("$[0].userName").value("Test User"))
                .andExpect(jsonPath("$[0].equipmentName").value("Microscope"));
    }

    @Test
    void createLoan_BlockedUser_ShouldReturn409() throws Exception {
        when(loanService.createLoan(any(Loan.class)))
                .thenThrow(new IllegalStateException("User is blocked and cannot borrow equipment"));

        mockMvc.perform(post("/api/loans")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testLoan)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("User is blocked and cannot borrow equipment"));
    }
}
