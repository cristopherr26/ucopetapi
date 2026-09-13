package com.uco.ucopetapi.dto.purchases;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PurchaseRequestDTO(
        @NotBlank(message = "El número de compra es obligatorio")
        String purchaseNumber,

        @NotNull(message = "El proveedor es obligatorio")
        UUID supplierId,

        @NotNull(message = "La sede es obligatoria")
        UUID headquarterId,

        Boolean hasDiscount,

        @NotEmpty(message = "La orden debe tener al menos un producto")
        @Valid
        List<PurchaseItemRequestDTO> items
) {
}
