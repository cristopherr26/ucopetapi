package com.uco.ucopetapi.dto.transfers;

import jakarta.validation.constraints.NotNull;

public record TransferStatusUpdateDTO(

        @NotNull(message = "El nuevo estado es obligatorio")
        TransferStatus status
) {
}
