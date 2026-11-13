package com.uco.ucopetapi.domain.provider;

import com.uco.ucopetapi.dto.person.DocumentType;
import jakarta.persistence.*;

import java.util.UUID;

@Entity
@Table(name = "providers")
public class ProviderDomain {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "provider_name", nullable = false, length = 100)
    private String providerName;

    @Column(name = "represent_name", nullable = false, length = 100)
    private String representName;

    @Enumerated(EnumType.STRING)
    @Column(name = "id_type", nullable = false, length = 10)
    private DocumentType idType;

    @Column(name = "document_number", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "mobile_number", length = 15)
    private String mobileNumber;

    @Column(name = "address", length = 150)
    private String address;

    @Column(name = "email", length = 120)
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

    private static String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
    private void normalize() {
        this.providerName = trimToNull(providerName);
        this.representName = trimToNull(representName);
        this.documentNumber = trimToNull(documentNumber);
        this.mobileNumber = trimToNull(mobileNumber);
        this.address = trimToNull(address);
        this.email = trimToNull(email);
    }
    public void validate() {
        normalize();

        if (id == null) {
            throw new ProviderValidationException("El id del proveedor es obligatorio");
        }
        if (providerName == null) {
            throw new ProviderValidationException("El nombre del proveedor es obligatorio");
        }
        if (providerName.length() > 100) {
            throw new ProviderValidationException("El nombre del proveedor no puede superar 100 caracteres");
        }
        if (representName == null) {
            throw new ProviderValidationException("El nombre del representante es obligatorio");
        }
        if (representName.length() > 100) {
            throw new ProviderValidationException("El nombre del representante no puede superar 100 caracteres");
        }
        if (idType == null) {
            throw new ProviderValidationException("El tipo de documento es obligatorio");
        }
        if (documentNumber == null) {
            throw new ProviderValidationException("El número de documento es obligatorio");
        }
        if (documentNumber.chars().anyMatch(Character::isWhitespace)) {
            throw new ProviderValidationException("El número de documento no puede tener espacios");
        }
        if (documentNumber.length() > 20) {
            throw new ProviderValidationException("El número de documento no puede superar 20 caracteres");
        }
        if (email != null && email.length() > 120) {
            throw new ProviderValidationException("El correo no puede superar 120 caracteres");
        }
        if (email != null && !email.matches("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
            throw new ProviderValidationException("El correo no es válido");
        }
        if (mobileNumber != null && !mobileNumber.matches("\\d{7,15}")) {
            throw new ProviderValidationException("El celular debe tener entre 7 y 15 dígitos");
        }
        if (address != null && address.length() > 150) {
            throw new ProviderValidationException("La dirección no puede superar 150 caracteres");
        }
    }

    public void updateWith(String providerName, String representName, DocumentType idType,
                           String documentNumber, String mobileNumber, String address, String email) {
        if (!active) {
            throw new ProviderValidationException("No se puede editar un proveedor inactivo");
        }
        this.providerName = providerName;
        this.representName = representName;
        this.idType = idType;
        this.documentNumber = documentNumber;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.email = email;
        validate();
    }
    public void deactivate() {
        if (!active) {
            throw new ProviderValidationException("El proveedor ya está inactivo");
        }
        this.active = false;
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
            ProviderDomain provider = new ProviderDomain(this);
            provider.validate();
            return provider;
        }
    }
}

