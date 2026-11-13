package com.uco.ucopetapi.service.receipts;

public class ReceiptStateConflictException extends RuntimeException {

    public ReceiptStateConflictException(final String message) {
        super(message);
    }
}