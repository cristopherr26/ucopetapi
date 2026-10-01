package com.uco.ucopetapi.dto.sale;

import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.domain.sale.ItemSaleDomain;
import com.uco.ucopetapi.dto.sale.enums.ItemType;

import java.util.UUID;

public final class ItemSaleDTO {

    private final UUID id;
    private final UUID saleOrderId;
    private final UUID itemId;
    private final ItemType itemType;
    private final TaxCategory taxCategory;
    private final Integer quantity;
    private final Integer unitPrice;
    private final Integer subtotal;

    public ItemSaleDTO(UUID id, UUID saleOrderId, UUID itemId, ItemType itemType,
                       TaxCategory taxCategory, Integer quantity, Integer unitPrice, Integer subtotal) {
        this.id = id;
        this.saleOrderId = saleOrderId;
        this.itemId = itemId;
        this.itemType = itemType;
        this.taxCategory = taxCategory;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public UUID getId() { return id; }
    public UUID getSaleOrderId() { return saleOrderId; }
    public UUID getItemId() { return itemId; }
    public ItemType getItemType() { return itemType; }
    public TaxCategory getTaxCategory() { return taxCategory; }
    public Integer getQuantity() { return quantity; }
    public Integer getUnitPrice() { return unitPrice; }
    public Integer getSubtotal() { return subtotal; }

    public ItemSaleDomain toDomain() {
        return new ItemSaleDomain(id, saleOrderId, itemId, itemType, taxCategory, quantity, unitPrice, subtotal);
    }

}
