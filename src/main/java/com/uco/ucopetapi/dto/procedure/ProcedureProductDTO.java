package com.uco.ucopetapi.dto.procedure;

public class ProcedureProductDTO {

    private String productCode;

    private Integer quantity;

    public ProcedureProductDTO() {
    }

    public ProcedureProductDTO(final String productCode, final Integer quantity) {
        setProductCode(productCode);
        setQuantity(quantity);
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
