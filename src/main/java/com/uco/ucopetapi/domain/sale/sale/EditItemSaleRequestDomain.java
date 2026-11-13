package com.uco.ucopetapi.domain.sale.sale;

import java.math.BigDecimal;

public class EditItemSaleRequestDomain {

    private Integer quantity;
    private BigDecimal unitPrice;

    public EditItemSaleRequestDomain() {}

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice;}

}
