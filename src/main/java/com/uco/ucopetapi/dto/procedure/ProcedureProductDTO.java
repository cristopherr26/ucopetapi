package com.uco.ucopetapi.dto.procedure;

import java.util.UUID;

public class ProcedureProductDTO {

    private UUID productId;

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