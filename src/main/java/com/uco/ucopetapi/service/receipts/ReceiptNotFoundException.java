package com.uco.ucopetapi.service.receipts;

import java.util.UUID;

public class ReceiptNotFoundException extends RuntimeException {

    public ReceiptNotFoundException(final UUID id) {
        super("Recibo no encontrado: " + id);
    }
}