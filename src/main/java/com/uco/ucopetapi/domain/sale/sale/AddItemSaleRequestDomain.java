package com.uco.ucopetapi.domain.sale.sale;

import com.uco.ucopetapi.domain.sale.enums.ItemType;

import java.math.BigDecimal;
import java.util.UUID;

public class AddItemSaleRequestDomain {

    private UUID itemId;
    private ItemType itemType;
    private Integer quantity;
    private BigDecimal unitPrice;

    public AddItemSaleRequestDomain() {
    }

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice;}

}
