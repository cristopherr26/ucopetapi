package com.uco.ucopetapi.dto.invoice;

import java.util.UUID;

public record InvoiceCreateMessageDTO(UUID invoiceId, UUID saleOrderId, String observations) {
}