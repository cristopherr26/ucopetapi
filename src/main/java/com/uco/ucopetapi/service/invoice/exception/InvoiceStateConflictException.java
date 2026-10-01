package com.uco.ucopetapi.service.invoice.exception;

public class InvoiceStateConflictException extends RuntimeException {
    public InvoiceStateConflictException(String message) {
        super(message);
    }
}
