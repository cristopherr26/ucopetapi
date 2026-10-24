package com.uco.ucopetapi.dto.sale;

import java.util.UUID;

public record ItemSaleDeleteMessageDTO(
        UUID itemSaleId,
        UUID saleOrderId
) {
}