package com.uco.ucopetapi.dto.sale;

import com.uco.ucopetapi.dto.sale.ItemSaleDTO;
import com.uco.ucopetapi.dto.sale.enums.SaleOrderState;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleOrderMessageDTO(
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
        List<ItemSaleDTO> items
) {
}
