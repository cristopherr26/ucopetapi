package com.uco.ucopetapi.dto.procedure;

import java.util.UUID;

public class ProcedureDTO {

    private UUID id;
    private String code;
    private String type;
    private String description;
    private Integer durationMinutes;
    private Boolean active;

    public ProcedureDTO() {
    }

    public ProcedureDTO(final UUID id, final String code, final String type, final String description,
                        final Integer durationMinutes, final boolean isActive) {
        setId(id);
        setCode(code);
        setType(type);
        setDescription(description);
        setDurationMinutes(durationMinutes);
        setActive(isActive);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(final String code) {
        this.code = code;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(final Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }
}
