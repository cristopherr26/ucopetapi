package com.uco.ucopetapi.domain.sale.sale;

import com.uco.ucopetapi.domain.sale.enums.SaleOrderState;
import com.uco.ucopetapi.domain.sale.itemSale.ItemSaleDomain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleOrderMessageDomain(
        UUID id,
        UUID headquarterId,
        String orderNumber,
        LocalDateTime saleDate,
        UUID clientID,
        UUID petId,
        UUID healthPlanId,
        BigDecimal subtotal,
        BigDecimal totalDiscount,
        BigDecimal totalTaxes,
        BigDecimal total,
        String comment,
        SaleOrderState state,
        List<ItemSaleDomain> items
) {
}
