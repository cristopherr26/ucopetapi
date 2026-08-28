package com.uco.ucopetapi.dto.certificate;

import java.util.UUID;

public class CertificateDTO {

    private UUID id;
    private String name;
    private String description;

    public CertificateDTO() {
    }

    public CertificateDTO(final UUID id, final String name, final String description) {
        setId(id);
        setName(name);
        setDescription(description);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }
}


