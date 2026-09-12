package com.uco.ucopetapi.dto.headquarter;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public class HeadquarterDTO {
    private UUID id;

    @NotBlank(message = "El nombre de la sede es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String name;

    @NotBlank(message = "La dirección de la sede es obligatoria")
    @Size(min = 3, max = 100, message = "La dirección debe tener entre 3 y 100 caracteres")
    private String address;

    private Boolean isActive;

    public HeadquarterDTO() {}

    public HeadquarterDTO(UUID id, String name, String address, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.isActive = isActive;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
