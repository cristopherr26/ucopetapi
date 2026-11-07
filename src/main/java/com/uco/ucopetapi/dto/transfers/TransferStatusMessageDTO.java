package com.uco.ucopetapi.dto.transfers;

import java.util.UUID;

public record TransferStatusMessageDTO(UUID id, TransferStatus status, UUID performedBy) {
}