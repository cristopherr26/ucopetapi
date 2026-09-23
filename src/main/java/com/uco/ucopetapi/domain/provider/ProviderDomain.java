package com.uco.ucopetapi.domain.provider;

import com.uco.ucopetapi.dto.person.DocumentType;
import jakarta.persistence.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false)
    private DocumentType idType;

    @Column(nullable = false)
    private String documentNumber;

    private String mobileNumber;

    private String address;

    private String email;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    protected ProviderDomain() {
    }

    private ProviderDomain(Builder builder) {
        this.id = builder.id;
        this.providerName = builder.providerName;
        this.representName = builder.representName;
        this.idType = builder.idType;
        this.documentNumber = builder.documentNumber;
        this.mobileNumber = builder.mobileNumber;
        this.address = builder.address;
        this.email = builder.email;
        this.active = builder.active;
    }

    public static Builder builder() {
        return new Builder();
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

    public DocumentType getIdType() {
        return idType;
    }

    public void setIdType(final DocumentType idType) {
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

    public static class Builder {
        private UUID id;
        private String providerName;
        private String representName;
        private DocumentType idType;
        private String documentNumber;
        private String mobileNumber;
        private String address;
        private String email;
        private boolean active;

        private Builder() {
        }

        public Builder id(UUID id) {
            this.id = id;
            return this;
        }

        public Builder providerName(String providerName) {
            this.providerName = providerName;
            return this;
        }

        public Builder representName(String representName) {
            this.representName = representName;
            return this;
        }

        public Builder idType(DocumentType idType) {
            this.idType = idType;
            return this;
        }

        public Builder documentNumber(String documentNumber) {
            this.documentNumber = documentNumber;
            return this;
        }

        public Builder mobileNumber(String mobileNumber) {
            this.mobileNumber = mobileNumber;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder active(boolean active) {
            this.active = active;
            return this;
        }

        public ProviderDomain build() {
            return new ProviderDomain(this);
        }
    }
}

