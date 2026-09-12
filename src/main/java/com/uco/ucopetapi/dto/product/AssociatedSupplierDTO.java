package com.uco.ucopetapi.dto.product;

import java.util.UUID;

public class AssociatedSupplierDTO {

    private UUID providerId;
    private Integer referencePrice;

    public AssociatedSupplierDTO(final UUID providerId, final Integer referencePrice) {
        this.providerId = providerId;
        this.referencePrice = referencePrice;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public Integer getReferencePrice() {
        return referencePrice;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public void setReferencePrice(Integer referencePrice) {
        this.referencePrice = referencePrice;
    }
}