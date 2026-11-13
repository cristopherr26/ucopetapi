package com.uco.ucopetapi.dto.product;

import java.util.UUID;

public record ProcessorReplyDTO(boolean applied, boolean duplicateName, String reason, UUID stockId, Integer quantity) {
}
