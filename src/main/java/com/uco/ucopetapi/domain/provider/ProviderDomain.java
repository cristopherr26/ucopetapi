package com.uco.ucopetapi.domain.provider;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "providers")
public class ProviderDomain {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false)
    private String providerName;

    @Column(nullable = false)
    private String representName;

    @Column(nullable = false)
    private UUID idType;

    @Column(nullable = false)
    private String documentNumber;

    private String mobileNumber;

    private String address;

    private String email;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected ProviderDomain() {
    }

    public ProviderDomain(final UUID id, final String providerName, final String representName,
                          final UUID idType, final String documentNumber, final String mobileNumber,
                          final String address, final String email, final boolean active) {
        this.id = id;
        this.providerName = providerName;
        this.representName = representName;
        this.idType = idType;
        this.documentNumber = documentNumber;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.email = email;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getProviderName() {
        return providerName;
    }

    public void setProviderName(final String providerName) {
        this.providerName = providerName;
    }

    public String getRepresentName() {
        return representName;
    }

    public void setRepresentName(final String representName) {
        this.representName = representName;
    }

    public UUID getIdType() {
        return idType;
    }

    public void setIdType(final UUID idType) {
        this.idType = idType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(final String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(final String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(final boolean active) {
        this.active = active;
    }
}

