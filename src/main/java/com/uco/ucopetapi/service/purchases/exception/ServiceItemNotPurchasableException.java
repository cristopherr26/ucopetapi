package com.uco.ucopetapi.service.purchases.exception;

public class ServiceItemNotPurchasableException extends RuntimeException {
    public ServiceItemNotPurchasableException(String name) {
        super("El servicio '" + name + "' no está disponible para compra a proveedores");
    }
}
