package com.uco.ucopetapi.service.purchases.exception;

public class PurchaseNumberNotFoundException extends RuntimeException {
    public PurchaseNumberNotFoundException(String purchaseNumber) {
        super("No existe una orden de compra con el número '" + purchaseNumber + "'");
    }
}
