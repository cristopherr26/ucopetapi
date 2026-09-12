package com.uco.ucopetapi.dto.purchases;

import com.uco.ucopetapi.domain.purchases.PurchaseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseResponseDTO(
        UUID id,
        String purchaseNumber,
        RelatedEntityDTO supplier,
        List<Item> items,
        LocalDateTime purchaseDate,
        BigDecimal subtotal,
        BigDecimal totalTaxes,
        BigDecimal total,
        PurchaseStatus status,
        UUID expenseId,
        UUID headquarterId,
        Boolean hasDiscount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record RelatedEntityDTO(UUID id, String name) {
    }

    public record Item(RelatedEntityDTO product, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }
}
