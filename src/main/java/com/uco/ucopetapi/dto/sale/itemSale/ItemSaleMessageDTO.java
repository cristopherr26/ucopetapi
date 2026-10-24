package com.uco.ucopetapi.dto.sale.itemSale;

import java.math.BigDecimal;
import java.util.UUID;

public record ItemSaleMessageDTO(
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