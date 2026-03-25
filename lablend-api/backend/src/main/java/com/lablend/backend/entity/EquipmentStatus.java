package com.lablend.backend.entity;

/**
 * Defines the possible functional states of a piece of laboratory equipment.
 * This enumeration is used to control business logic transitions 
 * and ensure data integrity across the reservation system.
 */
public enum EquipmentStatus {

    /**
     * The equipment is ready to be used and can be reserved by a user.
     */
    AVAILABLE,

    /**
     * The equipment is currently assigned to a user and cannot be reserved 
     * until it is returned or released.
     */
    RESERVED,

    /**
     * The equipment is undergoing repairs or calibration and is 
     * temporarily unavailable for use or reservation.
     */
    UNDER_MAINTENANCE
}