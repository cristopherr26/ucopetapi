package com.uco.ucopetapi.dto.purchases;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record LinkExpenseRequestDTO(
        @NotNull(message = "El id del egreso generado es obligatorio")
        UUID expenseId
) {
}
