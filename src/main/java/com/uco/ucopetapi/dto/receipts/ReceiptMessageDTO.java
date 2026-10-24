package com.uco.ucopetapi.dto.receipts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ReceiptMessageDTO(
        UUID receiptId,
        UUID tutorId,
        UUID petId,
        String concept,
        BigDecimal amount,
        UUID payMethodId,
        LocalDateTime date
) {
}
