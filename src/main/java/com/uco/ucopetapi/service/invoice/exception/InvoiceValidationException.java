package com.uco.ucopetapi.service.invoice.exception;

public class InvoiceValidationException extends RuntimeException {
    public InvoiceValidationException(String message) {
        super(message);
    }
}
