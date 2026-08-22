package com.uco.ucopetapi.dto.receipts;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptDTO(
        UUID id,
        String receiptNumber,
        UUID tutorId,
        UUID petId,
        String concept,
        Double amount,
        String paymentMethod,
        LocalDateTime date,
        String state
) {
}