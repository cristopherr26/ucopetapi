package com.uco.ucopetapi.dto.receipts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptResponseDTO(
        UUID id,
        String receiptNumber,
        UUID tutorId,
        UUID petId,
        String concept,
        BigDecimal amount,
        UUID payMethodId,
        String payMethodName,
        LocalDateTime date,
        ReceiptStatus state
) {
}