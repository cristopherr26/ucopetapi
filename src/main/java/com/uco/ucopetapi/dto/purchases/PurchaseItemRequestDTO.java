package com.uco.ucopetapi.dto.purchases;

import com.uco.ucopetapi.domain.purchases.ItemType;
import com.uco.ucopetapi.domain.purchases.TaxCategory;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseItemRequestDTO(
        @NotNull(message = "El producto es obligatorio")
        UUID productId,

        @NotNull(message = "El tipo de item es obligatorio")
        ItemType itemType,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer quantity,

        @NotNull(message = "El precio unitario es obligatorio")
        @Positive(message = "El precio unitario debe ser mayor a 0")
        BigDecimal unitPrice,

        @NotNull(message = "La categoría fiscal es obligatoria")
        TaxCategory taxCategory
) {
}
