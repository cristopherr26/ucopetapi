package com.uco.ucopetapi.dto.sale;

import java.math.BigDecimal;

public class EditItemSaleRequestDTO {

    private Integer quantity;
    private Integer unitPrice;

    public EditItemSaleRequestDTO() {
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }
}
