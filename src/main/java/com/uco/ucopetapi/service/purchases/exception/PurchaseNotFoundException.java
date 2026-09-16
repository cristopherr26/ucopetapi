package com.uco.ucopetapi.service.purchases.exception;

import java.util.UUID;

public class PurchaseNotFoundException extends RuntimeException {
    public PurchaseNotFoundException(UUID id) {
        super("No existe una orden de compra con id " + id);
    }
}
