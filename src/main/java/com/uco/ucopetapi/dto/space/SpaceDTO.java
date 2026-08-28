package com.uco.ucopetapi.dto.space;

import java.util.UUID;

public class SpaceDTO {
    private UUID id;
    private String type;
    private String description;
    private Boolean active;

    public SpaceDTO() {
    }

    public SpaceDTO(UUID id, String type, String description, Boolean active) {
        this.id = id;
        this.type = type;
        this.description = description;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}