package com.uco.ucopetapi.service.receipts;
public class ReceiptValidationException extends RuntimeException {

    public ReceiptValidationException(final String message) {
        super(message);
    }
}