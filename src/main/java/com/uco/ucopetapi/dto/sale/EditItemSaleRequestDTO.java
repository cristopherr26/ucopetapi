package com.uco.ucopetapi.dto.sale;

import java.math.BigDecimal;

public class EditItemSaleRequestDTO {

    private Integer quantity;
    private BigDecimal unitPrice;

    public EditItemSaleRequestDTO() {}

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

}
