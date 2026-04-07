package com.lablend.backend.dto;

import java.time.LocalDateTime;

/**
 * Data Transfer Object that represents a student's status within a queue.
 * Provides human-readable names and queue position for the frontend.
 */
public class WaitingListDTO {
    private String equipmentName;
    private String studentName;
    private LocalDateTime waitingSince;
    private int positionInQueue;

    public WaitingListDTO() {
    }

    public WaitingListDTO(String equipmentName, String studentName, LocalDateTime waitingSince, int positionInQueue) {
        this.equipmentName = equipmentName;
        this.studentName = studentName;
        this.waitingSince = waitingSince;
        this.positionInQueue = positionInQueue;
    }


    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public LocalDateTime getWaitingSince() {
        return waitingSince;
    }

    public void setWaitingSince(LocalDateTime waitingSince) {
        this.waitingSince = waitingSince;
    }

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public void setPositionInQueue(int positionInQueue) {
        this.positionInQueue = positionInQueue;
    }
}