package com.lablend.backend.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String type;
    
    @Enumerated(EnumType.STRING)
    private EquipmentStatus status;

    @Version
    private Long version;

    public Equipment() {
    }

    public Equipment(String name, String type, EquipmentStatus status) {
        this.name = name;
        this.type = type;
        this.status = status;
    }


    public void reserve() {
        if (this.status != EquipmentStatus.AVAILABLE) {
            throw new IllegalStateException("Solo se puede reservar equipo disponible. Estado actual: " + this.status);
        }
        this.status = EquipmentStatus.RESERVED;
    }

    public void startMaintenance() {
        if (this.status == EquipmentStatus.RESERVED) {
            throw new IllegalStateException("No se puede enviar a mantenimiento un equipo reservado.");
        }
        this.status = EquipmentStatus.UNDER_MAINTENANCE;
    }

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