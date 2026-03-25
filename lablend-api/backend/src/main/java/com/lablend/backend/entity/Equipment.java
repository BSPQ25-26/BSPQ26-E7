package com.lablend.backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

/**
 * Equipment entity that represents laboratory equipment and its lifecycle state.
 */
@Entity
public class Equipment {

    /**
     * Unique identifier of the equipment.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Human-readable equipment name.
     */
    @Column(name = "name", nullable = false)
    private String name;

    /**
     * Equipment type or category.
     */
    @Column(name = "type", nullable = false)
    private String type;
    
    /**
     * Current lifecycle status of the equipment.
     */
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    /**
     * Optimistic locking version used by JPA.
     */
    @Version
    @Column(name = "version")
    private Long version;


    /**
     * Default constructor required by JPA.
     */
    public Equipment() {
    }

    /**
     * Creates an equipment instance.
     *
     * @param name equipment name
     * @param type equipment type
     * @param status initial status
     */
    public Equipment(String name, String type, EquipmentStatus status) {
        this.name = name;
        this.type = type;
        this.status = status;
    }


    /**
     * Reserves this equipment when it is available.
     *
     * @throws IllegalStateException when the current status is not AVAILABLE
     */
    public void reserve() {
        if (this.status != EquipmentStatus.AVAILABLE) {
            throw new IllegalStateException("Solo se puede reservar equipo disponible. Estado actual: " + this.status);
        }
        this.status = EquipmentStatus.RESERVED;
    }

    /**
     * Moves this equipment to maintenance.
     *
     * @throws IllegalStateException when the equipment is currently reserved
     */
    public void startMaintenance() {
        if (this.status == EquipmentStatus.RESERVED) {
            throw new IllegalStateException("No se puede enviar a mantenimiento un equipo reservado.");
        }
        this.status = EquipmentStatus.UNDER_MAINTENANCE;
    }

    /**
     * Marks maintenance as completed and sets status to available.
     */
    public void finishMaintenance() {
        this.status = EquipmentStatus.AVAILABLE;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EquipmentStatus getStatus() {
        return status;
    }

    public void setStatus(EquipmentStatus status) {
        this.status = status;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() { 
        return version; 
    }
}