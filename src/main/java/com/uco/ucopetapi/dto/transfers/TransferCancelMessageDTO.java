package com.uco.ucopetapi.dto.transfers;

import java.util.UUID;

public record TransferCancelMessageDTO(UUID id, UUID performedBy) {
}