package com.uco.ucopetapi.dto.specialtie;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

/**
 * Solo tiene el constructor vacío a propósito: con un constructor con argumentos, Jackson 3 puede usarlo
 * como creador (los nombres de parámetro están disponibles) y exigir el boolean primitivo.
 * El estado se acepta como "active" o "isActive" y se devuelve como "active"; es opcional (null = sin cambio
 * en PATCH, true por defecto al crear).
 */
public class SpecialtieDTO {

    private UUID id;
    private String name;
    private String description;
    private Boolean active;
    private UUID certificate;

    public SpecialtieDTO() {
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public UUID getCertificate() {
        return certificate;
    }

    public void setCertificate(final UUID certificate) {
        this.certificate = certificate;
    }

    @JsonProperty("active")
    public Boolean isActive() {
        return active;
    }

    @JsonProperty("active")
    @JsonAlias("isActive")
    public void setActive(final Boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }
}
