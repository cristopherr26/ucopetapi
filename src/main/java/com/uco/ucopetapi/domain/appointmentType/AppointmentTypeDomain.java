package com.uco.ucopetapi.domain.appointmentType;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "appointment_types")
public class AppointmentTypeDomain {

    @Id
    private UUID id;
    private String name;
    private String description;
    private Boolean isActive;

    public AppointmentTypeDomain() {
        // Requerido por JPA para instanciar la entidad
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}