package com.uco.ucopetapi.dto.transfers;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransferResponseDTO(
        UUID id,
        RelatedEntityDTO originHeadquarter,
        RelatedEntityDTO destinationHeadquarter,
        RelatedEntityDTO product,
        Integer quantity,
        TransferStatus status,
        String observations,
        RelatedEntityDTO createdBy,
        LocalDateTime createdAt,
        RelatedEntityDTO updatedBy,
        LocalDateTime updatedAt
) {
    public record RelatedEntityDTO(UUID id, String name) {
    }
}