package com.uco.ucopetapi.dto.procedure;

import java.util.UUID;

public class ProcedureProductDTO {

    private UUID productId;

    private String productCode;

    private Integer quantity;

    public ProcedureProductDTO() {
    }

    public ProcedureProductDTO(final String productCode, final Integer quantity) {
        this(null, productCode, quantity);
    }

    public ProcedureProductDTO(final UUID productId, final String productCode, final Integer quantity) {
        setProductId(productId);
        setProductCode(productCode);
        setQuantity(quantity);
    }

    public UUID getProductId() {
        return productId;
    }

    public void setProductId(final UUID productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(final String productCode) {
        this.productCode = productCode;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }
}
