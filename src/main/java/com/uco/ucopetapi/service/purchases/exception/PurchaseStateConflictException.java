package com.uco.ucopetapi.service.purchases.exception;

public class PurchaseStateConflictException extends RuntimeException {
    public PurchaseStateConflictException(String message) {
        super(message);
    }
}
