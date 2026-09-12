package com.uco.ucopetapi.domain.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class ProcedureProductDomain {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(nullable = false)
    private Integer quantity;

    protected ProcedureProductDomain() {
        // Required by JPA.
    }

    public ProcedureProductDomain(final UUID productId, final Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public UUID getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }
}
