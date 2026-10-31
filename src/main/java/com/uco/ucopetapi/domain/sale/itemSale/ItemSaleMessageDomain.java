package com.uco.ucopetapi.domain.sale.itemSale;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemSaleMessageDomain(
        UUID id,
        UUID saleOrderId,
        UUID itemId,
        String itemType,
        String taxCategory,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal subtotal
) {
}