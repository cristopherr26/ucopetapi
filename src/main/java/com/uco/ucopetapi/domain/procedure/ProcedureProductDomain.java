package com.uco.ucopetapi.domain.procedure;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public class ProcedureProductDomain {

    @Column(name = "product_code", length = 20)
    private String productCode;

    @Column(nullable = false)
    private Integer quantity;

    protected ProcedureProductDomain() {
        // Required by JPA.
    }

    public ProcedureProductDomain(final String productCode, final Integer quantity) {
        this.productCode = productCode;
        this.quantity = quantity;
    }

    public String getProductCode() { return productCode; }
    public Integer getQuantity() { return quantity; }
}
