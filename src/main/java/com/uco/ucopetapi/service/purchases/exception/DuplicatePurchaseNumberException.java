package com.uco.ucopetapi.service.purchases.exception;

public class DuplicatePurchaseNumberException extends RuntimeException {
    public DuplicatePurchaseNumberException(String purchaseNumber) {
        super("Ya existe una orden de compra con el número '" + purchaseNumber + "'");
    }
}
