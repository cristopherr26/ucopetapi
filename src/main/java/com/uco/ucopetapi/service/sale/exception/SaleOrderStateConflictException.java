package com.uco.ucopetapi.service.sale.exception;

public class SaleOrderStateConflictException extends RuntimeException {
    public SaleOrderStateConflictException(String message) {
        super(message);
    }
}