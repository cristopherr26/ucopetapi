package com.uco.ucopetapi.exception.sale;

public class SaleOrderNotFoundException extends RuntimeException {
    public SaleOrderNotFoundException(String message) {
        super(message);
    }
}

