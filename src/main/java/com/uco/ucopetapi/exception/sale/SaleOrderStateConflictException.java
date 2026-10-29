package com.uco.ucopetapi.exception.sale;

public class SaleOrderStateConflictException extends RuntimeException {
    public SaleOrderStateConflictException(String message) {
        super(message);
    }
}