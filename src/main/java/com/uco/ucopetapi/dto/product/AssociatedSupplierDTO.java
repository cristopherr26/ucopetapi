package com.uco.ucopetapi.dto.product;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public class AssociatedSupplierDTO {

    @NotNull(message = "Cada proveedor asociado necesita un providerId")
    private UUID providerId;
    @PositiveOrZero(message = "El precio de referencia no puede ser negativo")
    private Integer referencePrice;

    public AssociatedSupplierDTO(final UUID providerId, final Integer referencePrice) {
        this.providerId = providerId;
        this.referencePrice = referencePrice;
    }

    public UUID getProviderId() {
        return providerId;
    }

    public void setProviderId(UUID providerId) {
        this.providerId = providerId;
    }

    public Integer getReferencePrice() {
        return referencePrice;
    }

    public void setReferencePrice(Integer referencePrice) {
        this.referencePrice = referencePrice;
    }
}