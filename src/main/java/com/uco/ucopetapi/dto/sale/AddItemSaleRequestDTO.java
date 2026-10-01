package com.uco.ucopetapi.dto.sale;

import com.uco.ucopetapi.dto.sale.enums.ItemType;

import java.util.UUID;

public class AddItemSaleRequestDTO {

    private UUID itemId;
    private ItemType itemType;
    private Integer quantity;
    private Integer unitPrice;

    public AddItemSaleRequestDTO() {
    }

    public UUID getItemId() { return itemId; }
    public void setItemId(UUID itemId) { this.itemId = itemId; }

    public ItemType getItemType() { return itemType; }
    public void setItemType(ItemType itemType) { this.itemType = itemType; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getUnitPrice() { return unitPrice; }
    public void setUnitPrice(Integer unitPrice) { this.unitPrice = unitPrice; }

}
