package com.uco.ucopetapi.dto.provider;

import java.util.UUID;

public class ProviderDTO {

    private UUID id;
    private String providerName;
    private String representName;
    private String idType;
    private String documentNumber;
    private String mobileNumber;
    private String address;
    private String email;
    private boolean isActive;

    public ProviderDTO(final UUID id, final String providerName, final String representName,
                       final String idType, final String documentNumber, final String mobileNumber,
                       final String address, final String email, final boolean isActive) {
        this.id = id;
        this.providerName = providerName;
        this.representName = representName;
        this.idType = idType;
        this.documentNumber = documentNumber;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.email = email;
        this.isActive = isActive;
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

    public String getIdType() {
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
}