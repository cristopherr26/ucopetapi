package com.uco.ucopetapi.dto.receipts;

import java.util.UUID;

public record ReceiptAcceptedDTO(UUID receiptId, String idMessage) {
}
