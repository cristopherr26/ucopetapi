package com.uco.ucopetapi.dto.attentionhealth;

import java.util.UUID;

/** Product reference and requested quantity for an attention. */
public class AttentionHealthProductDTO {

    private UUID productId;
    private Integer quantity;

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


