package com.uco.ucopetapi.dto.transfers;

import java.util.UUID;

public record TransferSaveMessageDTO(UUID id, UUID originHeadquarterId, UUID destinationHeadquarterId,
                                     UUID productId, Integer quantity, TransferStatus status,
                                     String observations, UUID performedBy) {
}