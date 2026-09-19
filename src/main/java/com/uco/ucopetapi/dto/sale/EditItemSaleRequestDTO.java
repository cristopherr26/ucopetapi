package com.uco.ucopetapi.dto.sale;

public class EditItemSaleRequestDTO {

    private Integer quantity;
    private Integer unitPrice;

    public EditItemSaleRequestDTO() {
    }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Integer unitPrice) { this.unitPrice = unitPrice; }
}
