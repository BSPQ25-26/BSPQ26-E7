package com.lablend.backend.entity;

/**
 * Possible statuses a {@link User} can have.
 * @version 1.0
 */
public enum UserStatus {
    /** Active user, can perform actions */
    ACTIVE,
    /** Blocked user, cannot borrow equipment */
    BLOCKED
}