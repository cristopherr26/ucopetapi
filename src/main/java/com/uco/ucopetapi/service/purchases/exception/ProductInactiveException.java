package com.uco.ucopetapi.service.purchases.exception;

public class ProductInactiveException extends RuntimeException {
    public ProductInactiveException(String name) {
        super("El producto '" + name + "' no está activo, no se puede comprar");
    }
}
