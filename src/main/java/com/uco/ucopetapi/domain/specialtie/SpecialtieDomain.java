package com.uco.ucopetapi.domain.specialtie;

import com.uco.ucopetapi.domain.certificate.CertificateDomain;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table (name = "Specialtie")
public class SpecialtieDomain {

    @Id
    @GeneratedValue
    private UUID id;
    private String name;
    private String description;
    private boolean isActive;

    @OneToOne
    private CertificateDomain certificate;

    public SpecialtieDomain() {
    }

    public SpecialtieDomain(final UUID id, final CertificateDomain certificate,
                            final String name, final boolean isActive, final String description) {
        setId(id);
        setCertificate(certificate);
        setName(name);
        setActive(isActive);
        setDescription(description);
    }

    public UUID getId() {
        return id;
    }

    public void setId(final UUID id) {
        this.id = id;
    }

    public CertificateDomain getCertificate() {
        return certificate;
    }

    public void setCertificate(final CertificateDomain certificate) {
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
