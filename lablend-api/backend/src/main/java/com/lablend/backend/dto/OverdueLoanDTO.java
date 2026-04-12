package com.lablend.backend.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for overdue loan reports.
 * Contains combined information from Loan, User, and Equipment.
 */
public class OverdueLoanDTO {
    private Long loanId;
    private String userName;
    private String userEmail;
    private String equipmentName;
    private LocalDateTime dueDate;

    public OverdueLoanDTO(Long loanId, String userName, String userEmail, String equipmentName, LocalDateTime dueDate) {
        this.loanId = loanId;
        this.userName = userName;
        this.userEmail = userEmail;
        this.equipmentName = equipmentName;
        this.dueDate = dueDate;
    }

    public Long getLoanId() { return loanId; }
    public String getUserName() { return userName; }
    public String getUserEmail() { return userEmail; }
    public String getEquipmentName() { return equipmentName; }
    public LocalDateTime getDueDate() { return dueDate; }
}