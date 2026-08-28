package com.uco.ucopetapi.dto.specialtie;

import com.uco.ucopetapi.dto.certificate.CertificateDTO;

import java.util.UUID;

public class SpecialtieDTO {

    private UUID id;
    private String name;
    private String description;
    private boolean isActive;
    private CertificateDTO certificate;

    public SpecialtieDTO() {
    }

    public SpecialtieDTO(final UUID id, final String name, final boolean isActive,
                         final String description, final CertificateDTO certificate) {
        setId(id);
        setName(name);
        setActive(isActive);
        setDescription(description);
        setCertificate(certificate);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public CertificateDTO getCertificate() {
        return certificate;
    }

    public void setCertificate(final CertificateDTO certificate) {
        this.certificate = certificate;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(final boolean active) {
        isActive = active;
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
