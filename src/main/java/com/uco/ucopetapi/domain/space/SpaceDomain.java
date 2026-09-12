package com.uco.ucopetapi.domain.space;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name="Space")
public class SpaceDomain {

    @Id
    private UUID id;

    @Column(name = "code")
    private String code;

    @Column(name = "type")
    private String type;

    @Column(name = "description")
    private String description;

    @Column(name = "active")
    private Boolean active;

    public SpaceDomain() {
    }

    public SpaceDomain(UUID id, String code, String type, String description, Boolean active) {
        setId(id);
        setCode(code);
        setType(type);
        setDescription(description);
        setActive(active);
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public Boolean getActive() {
        return active;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

}