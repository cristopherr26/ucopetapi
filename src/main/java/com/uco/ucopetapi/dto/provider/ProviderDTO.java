package com.uco.ucopetapi.dto.provider;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public final class ProviderDTO {

    private UUID id;

    @NotBlank(message = "Provider name is required")
    private String providerName;

    @NotBlank(message = "Representative name is required")
    private String representName;

    @NotBlank(message = "Document type is required")
    @Pattern(regexp = "NIT|CC|CE", message = "Document type must be NIT, CC or CE")
    private String idType;

    @NotBlank(message = "Document number is required")
    private String documentNumber;

    @NotBlank(message = "Mobile number is required")
    private String mobileNumber;

    @NotBlank(message = "Address is required")
    private String address;

    private boolean isActive;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid format")
    private String email;

    public ProviderDTO() {
        this.id = UUID.randomUUID();
        this.isActive = true;
    }

    public ProviderDTO(final UUID id, final String providerName, final String representName,
                       final String idType, final String documentNumber, final String mobileNumber,
                       final String address, final boolean isActive, final String email) {
        this.id = (id != null) ? id : UUID.randomUUID();
        this.providerName = providerName;
        this.representName = representName;
        this.idType = idType;
        this.documentNumber = documentNumber;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.isActive = isActive;
        this.email = email;
    }

    public UUID getId() { return id; }
    public void setId(final UUID id) { this.id = (id != null) ? id : UUID.randomUUID(); }

    public String getProviderName() { return providerName; }
    public void setProviderName(final String providerName) {
        this.providerName = (providerName != null) ? providerName.trim() : "";
    }

    public String getRepresentName() { return representName; }
    public void setRepresentName(final String representName) {
        this.representName = (representName != null) ? representName.trim() : "";
    }

    public String getIdType() { return idType; }
    public void setIdType(final String idType) {
        this.idType = (idType != null) ? idType.trim() : "";
    }

    public String getDocumentNumber() { return documentNumber; }
    public void setDocumentNumber(final String documentNumber) {
        this.documentNumber = (documentNumber != null) ? documentNumber.trim() : "";
    }

    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(final String mobileNumber) {
        this.mobileNumber = (mobileNumber != null) ? mobileNumber.trim() : "";
    }

    public String getAddress() { return address; }
    public void setAddress(final String address) {
        this.address = (address != null) ? address.trim() : "";
    }

    public boolean isActive() { return isActive; }
    public void setIsActive(final boolean isActive) { this.isActive = isActive; }

    public String getEmail() { return email; }
    public void setEmail(final String email) {
        this.email = (email != null) ? email.trim() : "";
    }
}