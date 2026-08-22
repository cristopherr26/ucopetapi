package com.uco.ucopetapi.dto.transfers;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record TransferRequestDTO(

        @NotNull(message = "La sede de origen es obligatoria")
        UUID originHeadquarterId,

        @NotNull(message = "La sede de destino es obligatoria")
        UUID destinationHeadquarterId,

        @NotNull(message = "El producto es obligatorio")
        UUID productId,

        @NotNull(message = "La cantidad es obligatoria")
        @Positive(message = "La cantidad debe ser mayor a 0")
        Integer quantity,

        @Size(max = 500, message = "Las observaciones no pueden superar 500 caracteres")
        String observations
) {
}