package com.uco.ucopetapi.dto.headquarter;

import java.util.UUID;

public class HeadquarterDTO {
    private UUID id;
    private String name;
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
