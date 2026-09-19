package com.uco.ucopetapi.service.sale.exception;

public class SaleOrderNotFoundException extends RuntimeException {
    public SaleOrderNotFoundException(String message) {
        super(message);
    }
}

