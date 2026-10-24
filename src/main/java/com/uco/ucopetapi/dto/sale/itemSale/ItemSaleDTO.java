package com.uco.ucopetapi.dto.sale.itemSale;

import com.uco.ucopetapi.domain.product.enums.TaxCategory;
import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDomain;
import com.uco.ucopetapi.dto.sale.enums.ItemType;

import java.math.BigDecimal;
import java.util.UUID;

public final class ItemSaleDTO {

    private final UUID id;
    private final UUID saleOrderId;
    private final UUID itemId;
    private final ItemType itemType;
    private final TaxCategory taxCategory;
    private final Integer quantity;
    private BigDecimal unitPrice;
    private final BigDecimal subtotal;

    public ItemSaleDTO(UUID id, UUID saleOrderId, UUID itemId, ItemType itemType,
                       TaxCategory taxCategory, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
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
    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice;}
    public BigDecimal getSubtotal() { return subtotal; }

    public ItemSaleDomain toDomain() {
        return new ItemSaleDomain(id, saleOrderId, itemId, itemType, taxCategory, quantity, unitPrice, subtotal);
    }

}
