package com.uco.ucopetapi.dto.purchases;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record PurchaseRequestDTO(
        @NotNull(message = "El proveedor es obligatorio")
        UUID supplierId,

        @NotEmpty(message = "La orden debe tener al menos un producto")
        @Valid
        List<PurchaseItemRequestDTO> items
) {
}
