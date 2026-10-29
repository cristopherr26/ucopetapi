package com.uco.ucopetapi.dto.invoice;

import java.util.UUID;

public record InvoiceAcceptedDTO(UUID invoiceId, String idMessage) {
}