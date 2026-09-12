package com.uco.ucopetapi.dto.procedure;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.UUID;

public class ProcedureProductDTO {

    @NotNull(message = "The product id is required")
    private UUID productId;

    @NotNull(message = "The quantity is required")
    @Positive(message = "The quantity must be greater than zero")
    private Integer quantity;

    public ProcedureProductDTO() {
    }

    public ProcedureProductDTO(final UUID productId, final Integer quantity) {
        setProductId(productId);
        setQuantity(quantity);
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(final UUID productId) {
        this.productId = productId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
}
