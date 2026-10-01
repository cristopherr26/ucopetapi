package com.uco.ucopetapi.dto.procedure;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.UUID;

public class ProcedureDTO {

    private UUID id;

    private String code;

    private String type;

    private String description;

    private Integer durationMinutes;

    private UUID spaceId;

    private List<ProcedureProductDTO> defaultProducts;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Boolean active;

    public ProcedureDTO() {
    }

    public ProcedureDTO(final UUID id, final String code, final String type, final String description,
                        final Integer durationMinutes, final UUID spaceId,
                        final List<ProcedureProductDTO> defaultProducts, final Boolean active) {
        setId(id);
        setCode(code);
        setType(type);
        setDescription(description);
        setDurationMinutes(durationMinutes);
        setSpaceId(spaceId);
        setDefaultProducts(defaultProducts);
        setActive(active);
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

    public UUID getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(final UUID spaceId) {
        this.spaceId = spaceId;
    }

    public List<ProcedureProductDTO> getDefaultProducts() {
        return defaultProducts;
    }

    public void setDefaultProducts(final List<ProcedureProductDTO> defaultProducts) {
        this.defaultProducts = defaultProducts;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(final Boolean active) {
        this.active = active;
    }
}
