package com.uco.ucopetapi.dto.receipts;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptRequestDTO(

        @NotNull(message = "El tutor es obligatorio")
        UUID tutorId,

        // Optional: not every receipt is tied to a specific pet
        // (e.g. a receipt for a store product bought by the tutor).
        UUID petId,

        @NotBlank(message = "El concepto es obligatorio")
        String concept,

        @NotNull(message = "El valor es obligatorio")
        @Positive(message = "El valor debe ser mayor a cero")
        BigDecimal amount,

        @NotNull(message = "El método de pago es obligatorio")
        UUID payMethodId,

        LocalDateTime date
) {
}
