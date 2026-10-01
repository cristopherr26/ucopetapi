package com.uco.ucopetapi.dto.provider;

import com.uco.ucopetapi.dto.person.DocumentType;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonPOJOBuilder;

import java.util.UUID;

@JsonDeserialize(builder = ProviderDTO.Builder.class)
public class ProviderDTO {

    private UUID id;
    private String providerName;
    private String representName;
    private DocumentType idType;
    private String documentNumber;
    private String mobileNumber;
    private String address;
    private String email;
    private boolean isActive;

    private ProviderDTO(Builder builder) {
        this.id = builder.id;
        this.providerName = builder.providerName;
        this.representName = builder.representName;
        this.idType = builder.idType;
        this.documentNumber = builder.documentNumber;
        this.mobileNumber = builder.mobileNumber;
        this.address = builder.address;
        this.email = builder.email;
        this.isActive = builder.isActive;
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

    public String getRepresentName() {
        return representName;
    }

    public DocumentType getIdType() {
        return idType;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public void setRepresentName(String representName) {
        this.representName = representName;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public void setIdType(DocumentType idType) {
        this.idType = idType;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {
        private UUID id;
        private String providerName;
        private String representName;
        private DocumentType idType;
        private String documentNumber;
        private String mobileNumber;
        private String address;
        private String email;
        private boolean isActive;

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

        public Builder isActive(boolean isActive) {
            this.isActive = isActive;
            return this;
        }

        public ProviderDTO build() {
            return new ProviderDTO(this);
        }
    }}