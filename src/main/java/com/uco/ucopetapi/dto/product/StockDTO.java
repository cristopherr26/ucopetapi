package com.uco.ucopetapi.dto.product;

import java.util.UUID;

public class StockDTO {

    @com.fasterxml.jackson.annotation.JsonInclude(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    private final UUID id;
    private final UUID productId;
    private final UUID headquarterId;
    private final Integer quantity;

    public StockDTO(final UUID id, final UUID productId, final UUID headquarterId, final Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.headquarterId = headquarterId;
        this.quantity = quantity;
    }

    public UUID getId() {
        return id;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getHeadquarterId() {
        return headquarterId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}