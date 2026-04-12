package com.lablend.backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity representing a student in the waiting list for a specific equipment.
 */
@Entity
@Table(name = "waiting_list")
public class WaitingList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long equipmentId;
    private LocalDateTime requestDate;

    public WaitingList() {
    }

    /** * Constructor to join the waiting list 
     * @param userId The ID of the student
     * @param equipmentId The ID of the equipment
     */
    public WaitingList(Long userId, Long equipmentId) {
        this.userId = userId;
        this.equipmentId = equipmentId;
        this.requestDate = LocalDateTime.now();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(Long equipmentId) {
        this.equipmentId = equipmentId;
    }

    public LocalDateTime getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
} 
    
