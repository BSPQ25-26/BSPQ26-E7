package com.lablend.backend.entity;

/**
 * Possible statuses of a {@link Loan}.
 */
public enum LoanStatus {
    /** The loan is currently active. */
    ACTIVE,
    /** The loan has been completed and equipment returned. */
    COMPLETED,
    /** The loan was cancelled before completion. */
    CANCELLED
}
