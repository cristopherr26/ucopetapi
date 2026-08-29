package com.uco.ucopetapi.dto.purchases;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseResponseDTO(
        UUID id,
        RelatedEntityDTO supplier,
        List<Item> items,
        BigDecimal totalValue,
        PurchaseStatus status,
        UUID expenseId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public record RelatedEntityDTO(UUID id, String name) {
    }

    public record Item(RelatedEntityDTO product, Integer quantity, BigDecimal unitPrice, BigDecimal subtotal) {
    }
}
