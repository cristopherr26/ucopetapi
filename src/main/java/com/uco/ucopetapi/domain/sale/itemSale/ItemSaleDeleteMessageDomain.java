package com.uco.ucopetapi.domain.sale.itemSale;

import java.util.UUID;

public record ItemSaleDeleteMessageDomain(
        UUID itemSaleId,
        UUID saleOrderId
) {
}