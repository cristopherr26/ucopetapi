package com.uco.ucopetapi.domain.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.util.UUID;

@Embeddable
public class ProcedureProductDomain {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_code", length = 20)
    private String productCode;

    @Column(nullable = false)
    private Integer quantity;

    protected ProcedureProductDomain() {
        // Required by JPA.
    }

    public ProcedureProductDomain(final String productCode, final Integer quantity) {
        this(null, productCode, quantity);
    }

    public ProcedureProductDomain(final UUID productId, final String productCode, final Integer quantity) {
        this.productId = productId;
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public UUID getProductId() { return productId; }
    public String getProductCode() { return productCode; }
    public Integer getQuantity() { return quantity; }
}
