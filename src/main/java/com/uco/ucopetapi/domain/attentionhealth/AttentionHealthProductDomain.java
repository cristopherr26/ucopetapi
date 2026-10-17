package com.uco.ucopetapi.domain.attentionhealth;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class AttentionHealthProductDomain {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    protected AttentionHealthProductDomain() {
        // Required by JPA.
    }

    public AttentionHealthProductDomain(final UUID productId, final Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}


