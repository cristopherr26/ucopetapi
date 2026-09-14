package com.uco.ucopetapi.domain.certificate;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "Certificate")
public class CertificateDomain {

    @Id
    private UUID id;
    private String name;
    private String description;

    public CertificateDomain() {
    }

    public CertificateDomain(final UUID id, final String description, final String name) {
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
