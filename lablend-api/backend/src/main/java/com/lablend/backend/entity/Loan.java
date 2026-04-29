package com.lablend.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;

import java.time.LocalDateTime;

/**
 * Entity representing a loan of laboratory equipment to a user.
 */
@Entity
@Table(name = "loans")
public class Loan {

    /** Unique identifier of the loan. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifier of the user who requested the loan. */
    @Column(name = "user_id", nullable = false)
    private Long userId;

    /** Identifier of the borrowed equipment. */
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    /** Date and time when the loan was created. */
    @Column(name = "loan_date", nullable = false)
    private LocalDateTime loanDate;

    /** Current status of the loan. */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LoanStatus status;

    /** Whether this loan has already been extended. Each loan can only be extended once. */
    @Column(name = "extended", nullable = false)
    private boolean extended = false;

    /** Default constructor required by JPA. */
    public Loan() {
    }

    /**
     * Creates a new loan with the given details.
     *
     * @param userId      identifier of the user
     * @param equipmentId identifier of the equipment
     * @param loanDate    date and time of the loan
     * @param status      initial loan status
     */
    public Loan(Long userId, Long equipmentId, LocalDateTime loanDate, LoanStatus status) {
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.loanDate = loanDate;
        this.status = status;
        this.extended = false;
    }

    /** @return the loan identifier */
    public Long getId() {
        return id;
    }

    /** @param id the loan identifier to set */
    public void setId(Long id) {
        this.id = id;
    }

    /** @return the user identifier */
    public Long getUserId() {
        return userId;
    }

    /** @param userId the user identifier to set */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /** @return the equipment identifier */
    public Long getEquipmentId() {
        return equipmentId;
    }

    /** @param equipmentId the equipment identifier to set */
    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    /** @return the loan date and time */
    public LocalDateTime getLoanDate() {
        return loanDate;
    }

    /** @param loanDate the loan date and time to set */
    public void setLoanDate(LocalDateTime loanDate) {
        this.loanDate = loanDate;
    }

    /** @return the current loan status */
    public LoanStatus getStatus() {
        return status;
    }

    /** @param status the loan status to set */
    public void setStatus(LoanStatus status) {
        this.status = status;
    }

    /** @return whether this loan has been extended */
    public boolean isExtended() {
        return extended;
    }

    /** @param extended whether the loan has been extended */
    public void setExtended(boolean extended) {
        this.extended = extended;
    }
}
