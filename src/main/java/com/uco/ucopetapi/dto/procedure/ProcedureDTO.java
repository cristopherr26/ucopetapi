package com.uco.ucopetapi.dto.procedure;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public class ProcedureDTO {

    private UUID id;

    @Size(max = 50, message = "The code must not exceed 50 characters")
    private String code;

    @NotBlank(message = "The type is required")
    @Size(max = 100, message = "The type must not exceed 100 characters")
    private String type;

    @NotBlank(message = "The description is required")
    @Size(max = 500, message = "The description must not exceed 500 characters")
    private String description;

    @NotNull(message = "The duration in minutes is required")
    @Positive(message = "The duration in minutes must be greater than zero")
    @Max(value = 1440, message = "The duration in minutes must not exceed 1440")
    private Integer durationMinutes;

    @NotEmpty(message = "At least one default product is required")
    @Valid
    private List<ProcedureProductDTO> defaultProducts;

    @NotNull(message = "The active status is required")
    private Boolean active;

    public ProcedureDTO() {
    }

    public ProcedureDTO(final UUID id, final String code, final String type, final String description,
                        final Integer durationMinutes, final List<ProcedureProductDTO> defaultProducts,
                        final Boolean active) {
        setId(id);
        setCode(code);
        setType(type);
        setDescription(description);
        setDurationMinutes(durationMinutes);
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
