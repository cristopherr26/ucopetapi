package com.uco.ucopetapi.dto.product;

import java.util.UUID;

public class AdjustStockRequestDTO {

    private UUID productId;
    private UUID headquarterId;
    private Integer quantity;

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(UUID productId) {
        this.productId = productId;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public void setHeadquarterId(UUID headquarterId) {
        this.headquarterId = headquarterId;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}